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

import com.pet_connect.backend_service.entity.Address;
import com.pet_connect.backend_service.entity.Post;

@Repository
public class PostDAO {

    private final DataSource dataSource;

    public PostDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Post addPost(Post post) {
        String addAddressSql = """
                INSERT INTO addresses (
                    country,
                    state,
                    city,
                    street,
                    zipcode
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        String addPostSql = """
                INSERT INTO posts (
                    user_id,
                    pet_id,
                    created_at,
                    title,
                    content,
                    type,
                    adoption_fee,
                    address_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Address address = post.getPickupLocation();

        if (address == null) {
            throw new IllegalArgumentException(
                    "Pickup location cannot be null.");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (
                        PreparedStatement stmt = conn.prepareStatement(
                                addAddressSql,
                                Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, address.getCountry());
                    stmt.setString(2, address.getState());
                    stmt.setString(3, address.getCity());
                    stmt.setString(4, address.getStreet());
                    stmt.setString(5, address.getZipCode());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows != 1) {
                        throw new SQLException(
                                "Creating address failed, affected rows: "
                                        + affectedRows);
                    }

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException(
                                    "Creating address failed, no ID obtained.");
                        }
                        address.setAddressId(
                                generatedKeys.getInt(1));
                    }
                }

                try (
                        PreparedStatement stmt = conn.prepareStatement(
                                addPostSql,
                                Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, post.getUserId());
                    stmt.setInt(2, post.getPetId());
                    stmt.setObject(3, post.getCreatedAt());

                    stmt.setString(4, post.getTitle());
                    stmt.setString(5, post.getContent());
                    stmt.setString(6, post.getType());

                    stmt.setBigDecimal(
                            7,
                            post.getAdoptionFee());

                    stmt.setInt(
                            8,
                            address.getAddressId());

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows != 1) {
                        throw new SQLException(
                                "Creating post failed, affected rows: "
                                        + affectedRows);
                    }

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException(
                                    "Creating post failed, no ID obtained.");
                        }

                        post.setPostId(
                                generatedKeys.getInt(1));
                    }
                }

                conn.commit();
                post.setPickupLocation(address);
                return post;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is about to be closed.
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while creating post.",
                    e);
        }
    }

    public List<Post> getAllPost() {
        List<Post> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    (
                        SELECT ph.url
                        FROM photos_of_pet pop
                        INNER JOIN photos ph
                            ON ph.photo_id = pop.photo_id
                        WHERE pop.pet_id = p.pet_id
                        ORDER BY ph.photo_id ASC
                        LIMIT 1
                    ) AS first_photo_url
                FROM posts p
                INNER JOIN addresses a
                    ON a.address_id = p.address_id
                ORDER BY p.created_at DESC, p.post_id DESC
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setPetId(rs.getInt("pet_id"));
                post.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setType(rs.getString("type"));
                post.setFirstPhotoUrl(rs.getString("first_photo_url"));
                Address pickupLocation = new Address();
                pickupLocation.setAddressId(rs.getInt("address_id"));
                pickupLocation.setStreet(rs.getString("street"));
                pickupLocation.setCity(rs.getString("city"));
                pickupLocation.setState(rs.getString("state"));
                pickupLocation.setCountry(rs.getString("country"));
                pickupLocation.setZipCode(rs.getString("zipcode"));
                post.setPickupLocation(pickupLocation);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while retrieving all posts.",
                    e);
        }
    }

    public List<Post> searchKeyword(String keyword) {
        List<Post> posts = new ArrayList<>();

        if (keyword == null || keyword.isBlank()) {
            return posts;
        }

        String sql = """
                SELECT
                    p.post_id,
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    (
                        SELECT ph.url
                        FROM photos_of_pet pop
                        INNER JOIN photos ph
                            ON ph.photo_id = pop.photo_id
                        WHERE pop.pet_id = p.pet_id
                        ORDER BY ph.photo_id ASC
                        LIMIT 1
                    ) AS first_photo_url
                FROM posts p
                INNER JOIN addresses a
                    ON a.address_id = p.address_id
                INNER JOIN pets pt
                    ON pt.pet_id = p.pet_id
                INNER JOIN breeds b
                    ON b.breed_id = pt.breed_id
                INNER JOIN species sp
                    ON sp.species_id = b.species_id
                WHERE
                    LOWER(sp.species_name) LIKE ?
                    OR LOWER(b.breed_name) LIKE ?
                    OR LOWER(p.type) LIKE ?
                    OR LOWER(a.street) LIKE ?
                    OR LOWER(a.city) LIKE ?
                    OR LOWER(a.state) LIKE ?
                    OR LOWER(a.zipcode) LIKE ?
                    OR LOWER(a.country) LIKE ?
                    OR LOWER(p.title) LIKE ?
                    OR LOWER(p.content) LIKE ?
                    OR CAST(p.post_id AS CHAR) LIKE ?
                    OR CAST(p.pet_id AS CHAR) LIKE ?
                ORDER BY
                    p.created_at DESC,
                    p.post_id DESC
                """;

        String keywordPattern = "%" + keyword.trim().toLowerCase() + "%";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int index = 1; index <= 12; index++) {
                stmt.setString(index, keywordPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();

                    post.setPostId(
                            rs.getInt("post_id"));
                    post.setUserId(
                            rs.getInt("user_id"));
                    post.setPetId(
                            rs.getInt("pet_id"));
                    post.setCreatedAt(
                            rs.getObject(
                                    "created_at",
                                    LocalDateTime.class));
                    post.setTitle(
                            rs.getString("title"));
                    post.setContent(
                            rs.getString("content"));
                    post.setType(
                            rs.getString("type"));
                    post.setFirstPhotoUrl(
                            rs.getString(
                                    "first_photo_url"));

                    Address pickupLocation = new Address();

                    pickupLocation.setAddressId(
                            rs.getInt("address_id"));
                    pickupLocation.setStreet(
                            rs.getString("street"));
                    pickupLocation.setCity(
                            rs.getString("city"));
                    pickupLocation.setState(
                            rs.getString("state"));
                    pickupLocation.setCountry(
                            rs.getString("country"));
                    pickupLocation.setZipCode(
                            rs.getString("zipcode"));

                    post.setPickupLocation(
                            pickupLocation);

                    posts.add(post);
                }
            }

            return posts;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while searching posts.",
                    e);
        }
    }

    public List<Post> getUserPosts(int userId) {
        List<Post> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    (
                        SELECT ph.url
                        FROM photos_of_pet pop
                        INNER JOIN photos ph
                            ON ph.photo_id = pop.photo_id
                        WHERE pop.pet_id = p.pet_id
                        ORDER BY ph.photo_id ASC
                        LIMIT 1
                    ) AS first_photo_url
                FROM posts p
                INNER JOIN addresses a
                    ON a.address_id = p.address_id
                WHERE user_id = ?
                ORDER BY p.created_at DESC, p.post_id DESC
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setPetId(rs.getInt("pet_id"));
                post.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setType(rs.getString("type"));
                post.setFirstPhotoUrl(rs.getString("first_photo_url"));
                Address pickupLocation = new Address();
                pickupLocation.setAddressId(rs.getInt("address_id"));
                pickupLocation.setStreet(rs.getString("street"));
                pickupLocation.setCity(rs.getString("city"));
                pickupLocation.setState(rs.getString("state"));
                pickupLocation.setCountry(rs.getString("country"));
                pickupLocation.setZipCode(rs.getString("zipcode"));
                post.setPickupLocation(pickupLocation);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while retrieving all posts.",
                    e);
        }
    }

    public List<Post> getSavedPostByUserId(int userId) {
        List<Post> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    (
                        SELECT ph.url
                        FROM photos_of_pet pop
                        INNER JOIN photos ph
                            ON ph.photo_id = pop.photo_id
                        WHERE pop.pet_id = p.pet_id
                        ORDER BY ph.photo_id ASC
                        LIMIT 1
                    ) AS first_photo_url
                FROM posts p
                INNER JOIN user_saves_post u
                    ON u.post_id = p.post_id
                INNER JOIN addresses a
                    ON a.address_id = p.address_id
                WHERE u.user_id = ?
                ORDER BY p.created_at DESC, p.post_id DESC
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setPetId(rs.getInt("pet_id"));
                post.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setType(rs.getString("type"));
                post.setFirstPhotoUrl(rs.getString("first_photo_url"));
                Address pickupLocation = new Address();
                pickupLocation.setAddressId(rs.getInt("address_id"));
                pickupLocation.setStreet(rs.getString("street"));
                pickupLocation.setCity(rs.getString("city"));
                pickupLocation.setState(rs.getString("state"));
                pickupLocation.setCountry(rs.getString("country"));
                pickupLocation.setZipCode(rs.getString("zipcode"));
                post.setPickupLocation(pickupLocation);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while retrieving all posts.",
                    e);
        }
    }

    public List<Post> getAppliedPostByUserId(int userId) {
        List<Post> posts = new ArrayList<>();

        String sql = """
                SELECT
                    p.post_id,
                    p.user_id,
                    p.pet_id,
                    p.created_at,
                    p.title,
                    p.content,
                    p.type,
                    a.address_id,
                    a.street,
                    a.city,
                    a.state,
                    a.country,
                    a.zipcode,
                    (
                        SELECT ph.url
                        FROM photos_of_pet pop
                        INNER JOIN photos ph
                            ON ph.photo_id = pop.photo_id
                        WHERE pop.pet_id = p.pet_id
                        ORDER BY ph.photo_id ASC
                        LIMIT 1
                    ) AS first_photo_url
                FROM posts p
                INNER JOIN user_applies_post u
                    ON u.post_id = p.post_id
                INNER JOIN addresses a
                    ON a.address_id = p.address_id
                WHERE u.user_id = ?
                ORDER BY p.created_at DESC, p.post_id DESC
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post();
                post.setPostId(rs.getInt("post_id"));
                post.setUserId(rs.getInt("user_id"));
                post.setPetId(rs.getInt("pet_id"));
                post.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setType(rs.getString("type"));
                post.setFirstPhotoUrl(rs.getString("first_photo_url"));
                Address pickupLocation = new Address();
                pickupLocation.setAddressId(rs.getInt("address_id"));
                pickupLocation.setStreet(rs.getString("street"));
                pickupLocation.setCity(rs.getString("city"));
                pickupLocation.setState(rs.getString("state"));
                pickupLocation.setCountry(rs.getString("country"));
                pickupLocation.setZipCode(rs.getString("zipcode"));
                post.setPickupLocation(pickupLocation);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error occurred while retrieving all posts.",
                    e);
        }
    }

    public boolean unsavePost(int userId, int postId) {
        String sql = """
                DELETE FROM user_saves_post
                WHERE user_id = ? AND post_id = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("fail to unsave post", e);
        }
    }

    public boolean unApplyPost(int userId, int postId) {
        String sql = """
                DELETE FROM user_applies_post
                WHERE user_id = ? AND post_id = ?
                """;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("fail to unapply post", e);
        }
    }

    public boolean deletePost(int postId) {
        String sql = """
                DELETE FROM posts
                WHERE post_id = ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("fail to delete post", e);
        }
    }

    public boolean savePost(int postId, int userId) {
        String sql = """
                INSERT INTO user_saves_post (
                    post_id,
                    user_id
                )
                VALUES(?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while saving post", e);
        }
    }

    public boolean applyPost(int postId, int userId) {
        String sql = """
                INSERT INTO user_applies_post (
                    post_id,
                    user_id
                )
                VALUES(?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            int affectedRow = stmt.executeUpdate();
            return affectedRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while saving post", e);
        }
    }
}
