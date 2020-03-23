package com.nmm.plugin.action.ui;

import com.intellij.database.model.RawConnectionConfig;
import com.intellij.database.psi.DbTable;
import com.intellij.openapi.project.Project;
import com.nmm.plugin.action.entity.ColumneProperties;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class DataSourcePasswordUI {

    private Project project;

    private JFrame frame;

    private JLabel error;
    private String sourceName;

    private DbTable table;

    private JTextField packageField;

    private Properties pwd;

    public DataSourcePasswordUI(Project project,String title, DbTable table) {
        this.project = project;
        this.table = table;
        this.sourceName = title;
        frame = new JFrame(title);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pwd = loadProperties(project);
        initUI(pwd.get(title) != null);
    }
    /**
     * 初始化页面
     * @param
     * @author nmm
     * @date 2020/3/20
     */
    private void initUI(boolean hasUserInfo) {
        frame.setSize(new Dimension(300,300));
        frame.setLocation(UIUtil.calcuCenterPoint(300,300));
        // 输入密码
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);
        packageField = new JTextField();

        int height = 30, labelWidth = 60, textWidth = 205;

        int y = 10;
        if (!hasUserInfo) {
            // 获取用户信息
            // 输入框
            JLabel label = new JLabel("用户名：");
            label.setBounds(10,y,labelWidth,height);
            panel.add(label);
            JTextField username = new JTextField();
            username.setBounds(75,y,textWidth,height);
            panel.add(username);
            //密码
            JLabel plabel = new JLabel("密码：");
            plabel.setBounds(10,y+35,labelWidth,height);
            panel.add(plabel);
            JPasswordField passwordField = new JPasswordField();
            passwordField.setBounds(75,y+35,textWidth,height);
            panel.add(passwordField);

            // 包名y+70
            JLabel pkgLabel = new JLabel("包名：");
            pkgLabel.setBounds(10,y+70,labelWidth,height);
            panel.add(pkgLabel);
            packageField.setBounds(75,y+70,textWidth,height);
            panel.add(packageField);


            JButton testConn = new JButton("测试连接");
            testConn.setBackground(Color.gray);
            testConn.setBounds(10,y+105,100,height);
            panel.add(testConn);

            JButton sure = new JButton("生成");
            sure.setBackground(Color.gray);
            sure.setBounds(120,y+105,60,height);
            panel.add(sure);

            error = new JLabel();
            error.setForeground(Color.red);
            error.setBounds(10,y+145,280,60);
            panel.add(error);

            addListeners(testConn,sure,username,passwordField);
        } else {
            JLabel pkgLabel = new JLabel("包名：");
            pkgLabel.setBounds(10,y,labelWidth,height);
            panel.add(pkgLabel);
            packageField.setBounds(75,y,textWidth,height);
            panel.add(packageField);

            JButton sure = new JButton("生成");
            sure.setBackground(Color.gray);
            sure.setBounds(10,y+35,60,height);
            panel.add(sure);
            error = new JLabel();
            error.setForeground(Color.red);
            error.setBounds(10,y+80,280,60);
            panel.add(error);
            addCreateListeners(sure);
        }
    }
    /**
     * 生成代码
     * @param sure
     * @author nmm
     * @date 2020/3/21
     */
    private void addCreateListeners(JButton sure) {
        sure.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 获取用户名密码
                String pwdinfo = new String(Base64.getDecoder().decode(pwd.getProperty(sourceName)));
                int index = pwdinfo.indexOf("@");
                if (index < 0) {
                    error.setText("获取连接信息失败！");
                    return;
                }
                String username = pwdinfo.substring(0,index);
                String password = pwdinfo.substring(index + 1);
                generatorFile(username,password);
            }

        });
    }
    /**
     * 生成代码
     * @param username
     * @param password
     * @author nmm
     * @date 2020/3/21
     */
    private void generatorFile(String username, String password) {
        Pattern pattern = Pattern.compile("^([a-z]+\\.)*[a-z]+$");
        if (!pattern.matcher(packageField.getText()).find()) {
            error.setText("包名非法");
            return;
        }
        error.setText("");
        Map<String,Object> dataMap = new HashMap<>();
        String packageName = packageField.getText();
        dataMap.put("basePackage",packageName);
        dataMap.put("createTime", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String tablename = table.getName();
        String entityName = createEntityName(tablename);
        dataMap.put("entityName",entityName);
        dataMap.put("tablename",tablename);
        dataMap.put("entitypath",entityName.toLowerCase());
        dataMap.put("entityVar",entityName.substring(0,1).toLowerCase() + entityName.substring(1));


        String sachema = table.getParent().getName();
        String driverClass = table.getDataSource().getDelegate().getConnectionConfig().getDriverClass();
        String url = getConnectionUrl(sachema);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List<ColumneProperties> columns = new ArrayList<ColumneProperties>();
        dataMap.put("fields",columns);

        try{
            DriverManager.registerDriver((Driver) Class.forName(driverClass).getConstructor().newInstance());
            connection = DriverManager.getConnection(url,username,password);
            preparedStatement = connection.prepareStatement("select * from " + tablename);
            rs = preparedStatement.executeQuery("show full columns from " + tablename);
            Boolean hasdate = null;
            while (rs.next()) {
                ColumneProperties column = new ColumneProperties();
                String field = createEntityName(rs.getString("Field"));
                column.setName(field.substring(0,1).toLowerCase() + field.substring(1));
                String type = rs.getString("Type").toLowerCase();

                if (type.startsWith("int")) {
                    column.setType("int");
                } else if (type.startsWith("float") || type.startsWith("double") || type.startsWith("decimal")) {
                    column.setType("number");
                } else if (type.startsWith("varchar") || type.startsWith("text")) {
                    column.setType("string");
                } else if (type.startsWith("tinyint")) {
                    column.setType("boolean");
                } else if (type.startsWith("datetime") || type.startsWith("timestamp")) {
                    hasdate = true;
                    column.setType("time");
                } else if (type.startsWith("date")) {
                    hasdate = true;
                    column.setType("date");
                } else {
                    System.out.println(type);
                    column.setType("other");
                }
                column.setComment(rs.getString("Comment"));
                columns.add(column);
            }
            dataMap.put("hasDate",hasdate);
            writeFile(dataMap);
            frame.setVisible(false);
        }catch (Exception e) {
            e.printStackTrace();
            error.setText("生成失败！");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 生成文件
     * @param dataMap
     * @author nmm
     * @date 2020/3/21
     */
    private void writeFile(Map<String, Object> dataMap) throws IOException, TemplateException {
        Configuration configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File(this.getClass().getClassLoader().getResource("templates/mybatis").getFile()));
        //构建基础dir
        File baseDir = new File(project.getBasePath());
        File basePkg = new File(baseDir,"src/main/java" + File.separator + dataMap.get("basePackage").toString().replace(".","/"));
        basePkg.mkdirs();
        // 创建资源文件
        File baseRes = new File(baseDir,"src/main/resources/mapper");
        baseRes.mkdirs();
        //生成controller
        generatorJava(configuration,basePkg,dataMap,"Controller");
        generatorJava(configuration,basePkg,dataMap,"Service");
        generatorJava(configuration,basePkg,dataMap,"Mapper");
        generatorJava(configuration,basePkg,dataMap,"Entity");
        //生成xml
        Template xmltmp = configuration.getTemplate("mapperxml.ftl");
        FileWriter writer = new FileWriter(new File(baseRes,dataMap.get("entityName") + "Mapper.mxl"));
        xmltmp.process(dataMap,writer);
        writer.close();

    }

    /**
     * 生成文件
     * @param configuration
     * @param basePkg
     * @param entityName
     * @param filename
     * @throws IOException
     */
    private void generatorJava(Configuration configuration, File basePkg, Map<String, Object> entityName, String filename) throws IOException {
        File controllerfile = new File(basePkg,filename.toLowerCase() + "/");
        controllerfile.mkdirs();
        controllerfile = new File(controllerfile,entityName.get("entityName") + filename + ".java");
        Template controllerTmp = configuration.getTemplate(filename + ".ftl");
        FileWriter writer = new FileWriter(controllerfile);
        try {
            controllerTmp.process(entityName,writer);
            writer.close();
        } catch (TemplateException e) {
            e.printStackTrace();
            error.setText("生成java文件失败！");
        }
    }

    /**
     * 创建实体名称
     * @param tablename
     * @return
     */
    private String createEntityName(String tablename) {
        String[] names = tablename.split("_");
        StringBuilder entity = new StringBuilder();
        for (String name : names) {
            entity.append(name.substring(0,1).toUpperCase()).append(name.substring(1));
        }
        return entity.toString();
    }

    /**
     * 获取连接串
     * @param sachema
     * @author nmm
     * @date 2020/3/21
     */
    private String getConnectionUrl(String sachema) {
        String url = table.getDataSource().getDelegate().getConnectionConfig().getUrl();
        if (url.indexOf(sachema) < 0) {
            url += "/" + sachema + "?";
        } else if (url.indexOf("?") <0 ) {
            url += "?";
        }
        if (url.indexOf("characterEncoding") < 0) {
            url += "&characterEncoding=UTF8";
        }
        if (url.indexOf("useSSL") < 0) {
            url += "&useSSL=false";
        }
        if (url.indexOf("serverTimezone") < 0) {
            url += "&serverTimezone=Asia/Shanghai";
        }
        if (url.indexOf("useUnicode") < 0) {
            url += "&useUnicode=true";
        }
        System.out.println(url);
        return url;
    }

    /**
     * 添加事件
     * @param testConn
     * @param sure
     * @param username
     * @param passwordField
     * @author nmm
     * @date 2020/3/20
     */
    private void addListeners(JButton testConn, JButton sure, JTextField username, JPasswordField passwordField) {
        testConn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String user = username.getText();
                String password = new String(passwordField.getPassword());
                error.setForeground(Color.red);
                if (user == null || "".equalsIgnoreCase(user.trim())) {
                    error.setText("请输入用户名！");
                    return;
                }
                if (password == null || "".equalsIgnoreCase(password.trim())) {
                    error.setText("请输入密码！");
                    return;
                }
                // 尝试获取连接，目前只考虑mysql
                try {
                    RawConnectionConfig connectionConfig = table.getDataSource().getConnectionConfig();
                    DriverManager.registerDriver((Driver) Class.forName(connectionConfig.getDriverClass()).getConstructor(null).newInstance(null));
                    Connection conn = DriverManager.getConnection(connectionConfig.getUrl(),user,password);
                    if (conn == null) {
                        error.setText("连接失败!");
                    } else {
                        error.setForeground(Color.green);
                        error.setText("连接成功！");
                        conn.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        });
        sure.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String user = username.getText();
                String password = new String(passwordField.getPassword());
                error.setForeground(Color.red);
                if (user == null || "".equalsIgnoreCase(user.trim())) {
                    error.setText("请输入用户名！");
                    return;
                }
                if (password == null || "".equalsIgnoreCase(password.trim())) {
                    error.setText("请输入密码！");
                    return;
                }
                writepwd(project,sourceName,Base64.getEncoder().encodeToString((user + "@" + password).getBytes()));
                generatorFile(user,password);
            }
        });
    }

    public void show() {
        frame.setVisible(true);
    }

    /**
     * 保存密码
     * @param pwd
     * @author nmm
     * @date 2020/3/19
     */
    private void writepwd(Project project, String sourceName,String pwd) {
        File file = new File(project.getBasePath(),".idea/mybatisinfo.data");
        try {
            FileWriter writer = new FileWriter(file,true);
            writer.write(sourceName + "=" + pwd);
            writer.write('\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取密码
     * @param project
     * @return
     */
    private Properties loadProperties(Project project) {
        File file = new File(project.getBasePath(),".idea/mybatisinfo.data");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) {
        new DataSourcePasswordUI(null,"gateway", null).show();
    }

}
