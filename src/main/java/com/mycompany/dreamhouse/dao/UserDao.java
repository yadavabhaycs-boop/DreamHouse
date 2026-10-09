package com.mycompany.dreamhouse.dao;
import com.mycompany.dreamhouse.model.User; import com.mycompany.dreamhouse.util.Database; import java.sql.*;
public class UserDao {
 public User findByEmail(String email)throws SQLException{String sql="SELECT id,name,email,role,password_hash FROM users WHERE email=?"; try(Connection c=Database.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,email);try(ResultSet r=s.executeQuery()){return r.next()?new User(r.getLong("id"),r.getString("name"),r.getString("email"),r.getString("role"),r.getString("password_hash")):null;}}}
 public void createUser(String name,String email,String hash)throws SQLException{String sql="INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,'USER')";try(Connection c=Database.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setString(1,name);s.setString(2,email);s.setString(3,hash);s.executeUpdate();}}
}
