package com.nmm.plugin.action;

import com.intellij.openapi.project.Project;
import com.mysql.cj.jdbc.Driver;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TemplateBuilder {

    private static String templatePath = "templates";
    private Configuration configuration;
    public TemplateBuilder() {
        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setEncoding(Locale.getDefault(),"UTF-8");
        //读取内容。
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        configuration.setTemplateLoader(templateLoader);

        loaderTemplate(templateLoader);
    }

    private void loaderTemplate(StringTemplateLoader templateLoader) {
        // 读取配置文件
        String[] filenames = {"Application.ftl","applicationenv.ftl","applicationyml.ftl","pom.ftl","Hello.ftl"};
        for (String filename : filenames) {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("templates/" + filename);
            try{
                LineNumberReader reader = new LineNumberReader(new InputStreamReader(in,"UTF-8"));
                StringBuilder line = new StringBuilder();
                String content = null;
                while ((content = reader.readLine()) != null) {
                    line.append(content).append("\n");
                }
                templateLoader.putTemplate(filename,line.toString());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 初始化项目文件
     * @param
     * @author nmm
     * @date 2020/3/18
     */
    public String initProjectFile(Project project,String packageName) throws IOException {
        //构建基础dir
        File baseDir = new File(project.getBasePath());

        boolean res = initPom(baseDir,configuration);
        if (!res) {
            return "生成pom文件失败！";
        }
        // 构建基本包目录
        File basePkg = new File(baseDir,"src/main/java" + File.separator + packageName.replace(".","/"));
        if (!basePkg.exists()) {
            basePkg.mkdirs();
        }
        //创建包
        res = initPackages(basePkg,configuration,packageName);
        if (!res) {
            return "创建文档目录失败！";
        }
        // 创建资源文件
        File baseRes = new File(baseDir,"src/main/resources");
        baseRes.mkdirs();
        //创建mapper目录
        res = initResources(baseRes,configuration,project);
        if (!res) {
            return "创建资源文件！";
        }
        return "success";
    }
    /**
     * 初始化pom
     * @param configuration
     * @return
     */
    private boolean initPom(File baseDir, Configuration configuration) throws IOException {
        boolean res = true;
        // 首先读取项目下pom
        File currpom = new File(baseDir + File.separator + "pom.xml");
        Map<String,String> projectInfo = new HashMap<>();
        projectInfo.put("flag","$");
        //读取参数
        LineNumberReader reader = new LineNumberReader(new FileReader(currpom));
        String[] keys = {"modelVersion","groupId","artifactId","version"};
        String line = null;
        boolean hasInit = false;
        while ((line = reader.readLine() ) != null) {
            if (line.trim().contains("dependencies")) {
                hasInit = true;
            }
            for (String key : keys) {
                if (line.trim().startsWith("<" + key)) {
                    projectInfo.put(key,line.trim());
                    break;
                }
            }
        }
        try{
            reader.close();
        }catch (Exception e){
            res =false;
        }
        if (hasInit) {
            //已经完成初始化，不需要处理
            return res;
        }
        //读取了参数
        FileWriter writer = new FileWriter(currpom);
        //获取模板
        Template template = configuration.getTemplate("pom.ftl","UTF-8");
        try {
            System.out.println(projectInfo);
            template.process(projectInfo,writer);
        } catch (TemplateException e) {
            writer.flush();
            writer.close();
            res = false;
        }

        return res;
    }
    /**
     * 初始化包目录
     * @param basePkg
     * @param configuration
     * @param packageName
     * @author nmm
     * @date 2020/3/18
     */
    private boolean initPackages(File basePkg, Configuration configuration, String packageName) throws IOException {
        boolean res = true;
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("packageName",packageName);
        //创建各个包目录
        new File(basePkg,"controller").mkdirs();
        new File(basePkg,"service").mkdirs();
        new File(basePkg,"mapper").mkdirs();
        new File(basePkg,"entity").mkdirs();
        new File(basePkg,"api").mkdirs();

        File applicationFile = new File(basePkg,"Application.java");
        if (applicationFile.exists()) {
            //已经存在不需要处理
            return res;
        }
        // 输出启动文件
        Template application = configuration.getTemplate("Application.ftl","UTF-8");
        FileWriter writer = new FileWriter(applicationFile);
        try {
            application.process(dataMap,writer);
        } catch (TemplateException e) {
            writer.flush();
            writer.close();
            res = false;
        }

        return res;
    }

    /**
     * 创建资源目录
     * @param baseRes
     * @param configuration
     * @param project
     * @return
     */
    private boolean initResources(File baseRes, Configuration configuration, Project project) throws IOException {
        boolean res = true;
        // 创建mapper目录
        new File(baseRes,"mapper").mkdirs();
        //数据参数
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("projectName",project.getName());

        File applicationFile = new File(baseRes,"application.yml");
        if (applicationFile.exists()) {
            //已经存在不需要处理
            return res;
        }

        // 获取模板
        Template maintmp = configuration.getTemplate("applicationyml.ftl","UTF-8");
        FileWriter mainwriter = new FileWriter(applicationFile);
        try {
            maintmp.process(dataMap,mainwriter);
        } catch (TemplateException e) {
            res = false;
            mainwriter.flush();
            mainwriter.close();
        }
        //写出环境文件
        Template envtmp = configuration.getTemplate("applicationenv.ftl","UTF-8");
        String filenames[] = {"application-dev.yml","application-test.yml","application-prod.yml"};
        for (String filename : filenames) {
            mainwriter = new FileWriter(new File(baseRes,filename));
            try {
                envtmp.process(dataMap,mainwriter);
            } catch (TemplateException e) {
                res = false;
                mainwriter.flush();
                mainwriter.close();
            }
        }
        return res;
    }

    public static void main(String[] args) throws IOException, TemplateException, SQLException {
        //测试生成

        Template template = new TemplateBuilder().configuration.getTemplate("Hello.ftl");
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("package","com.haier.jsjg");
        dataMap.put("className","HelloWorld");

        DriverManager.registerDriver(new Driver());
        String url = "jdbc:mysql://rm-m5eqd9cju0wl3f26w.mysql.rds.aliyuncs.com:3306/gateway-test?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai";
        Connection conn = DriverManager.getConnection(url,"gateway_test","CRlSkos1AN");
        PreparedStatement ps = conn.prepareStatement("select * from api_info");
        ResultSet rs = ps.executeQuery("show full columns from api_info");

        String comment = "";
        while (rs.next()) {
            comment += rs.getString("Comment");
        }

        dataMap.put("test",comment);

        template.process(dataMap,new FileWriter("F:/Hello.java"));

    }
}
