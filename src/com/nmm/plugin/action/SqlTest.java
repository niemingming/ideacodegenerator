package com.nmm.plugin.action;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;

public class SqlTest {

    public static void main(String[] args) throws SQLException {
        DriverManager.registerDriver(new Driver());

        Connection conn = DriverManager.getConnection("jdbc:mysql://rm-m5eqd9cju0wl3f26w.mysql.rds.aliyuncs.com:3306/gateway-test?characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai","gateway_test","CRlSkos1AN");
        //获取元数据
        ResultSet rs =conn.prepareStatement("select * from api_info").executeQuery("show full columns from api_info");

        while (rs.next()){
            System.out.println(rs.getString("Comment"));
        }
    }
}
