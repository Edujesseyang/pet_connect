package com.pet_connect.backend_service.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.pet_connect.backend_service.entity.Conversation;
import com.pet_connect.backend_service.entity.Message;

@Repository
public class MessageDAO {
    private final DataSource dataSource;

    public MessageDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int createConversation(Conversation con) {
        String sql = """
                INSERT INTO conversations (
                    from_uid,
                    to_uid,
                    pet_id
                )
                VALUES (?, ?, ?)
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
            stmt.setInt(1, con.getFromUser());
            stmt.setInt(2, con.getToUser());
            stmt.setInt(3, con.getPetId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                return -1;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                return -1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while creating conversation", e);
        }
    }

    public Conversation sendMessage(Message msg) { // todo
        String sql = """
                INSERT INTO messages (
                    content,
                    sender_id,
                    sent_at,
                    conversation_id
                )
                VALUES(?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, msg.getContent());
            stmt.setInt(2, msg.getSenderId());
            stmt.setObject(3, LocalDateTime.now());
            stmt.setInt(4, msg.getConversationId());
            int affectedRow = stmt.executeUpdate();
            if (affectedRow != 1) {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("fail to send message", e);
        }
        Conversation conv = getConversation(msg.getConversationId());
        return conv;
    }

    public Conversation getConversation(int conId) {
        try (Connection conn = dataSource.getConnection()) {
            return getFullConversation(conn, conId);
        } catch (SQLException e) {
            throw new RuntimeException("fail to get conversation", e);
        }

    }

    public Conversation getFullConversation(Connection conn, int conId) {
        String sql = """
                SELECT
                    from_uid,
                    to_uid,
                    pet_id
                FROM conversations
                WHERE conversation_id = ?
                """;

        Conversation con = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    con = new Conversation();
                    con.setConversationId(conId);
                    con.setFromUser(rs.getInt("from_uid"));
                    con.setToUser(rs.getInt("to_uid"));
                    con.setPetId(rs.getInt("pet_id"));
                    List<Message> msgs = getAllMessage(conn, conId);
                    con.setMessages(msgs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while geting conversation", e);
        }
        return con;
    }

    public List<Message> getAllMessage(Connection conn, int conId) {
        List<Message> res = new ArrayList<>();

        String sql = """
                  SELECT
                    m.message_id,
                    m.content,
                    m.sender_id,
                    m.sent_at,
                    u.fullname
                FROM messages m
                JOIN users u
                ON u.user_id = m.sender_id
                WHERE m.conversation_id = ?
                ORDER BY m.sent_at;
                                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conId);

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message msg = new Message();
                    msg.setContent(rs.getString("content"));
                    msg.setSenderId(rs.getInt("sender_id"));
                    msg.setSenderName(rs.getString("fullname"));
                    msg.setTimestamp(rs.getObject("sent_at", LocalDateTime.class));
                    res.add(msg);
                }
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException("get all message fail", e);
        }
    }

    public List<Conversation> getAllConversation(int userId) {
        String sql = """
                SELECT conversation_id
                FROM conversations
                WHERE from_uid = ? OR to_uid = ?
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            List<Conversation> cons = new ArrayList<>();
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int conId = rs.getInt("conversation_id");
                    Conversation cur = getFullConversation(conn, conId);
                    cons.add(cur);
                }
            }
            return cons;
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while getting all conversation", e);
        }
    }

    public boolean endConversation(int conversationId) {
        String sql = """
                DELETE FROM conversations
                WHERE conversation_id = ?
                """;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversationId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while deleting conversation", e);
        }

    }
}
