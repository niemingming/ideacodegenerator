package com.nmm.plugin.action;

import com.intellij.ide.plugins.newui.InstallButton;
import com.intellij.openapi.project.Project;
import com.nmm.plugin.action.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 自定义界面
 */
public class InitProjectForm {

    private Project project;

    private JFrame frame;
    //计算屏幕宽度和高度

    public InitProjectForm(Project project) {
        this.project = project;
        init();
    }

    private void init() {
        frame = new JFrame();
        frame.setResizable(false);
        if (project != null) {
            frame.setTitle(project.getName());
        } else {
            frame.setTitle("测试");
        }
        Dimension size = new Dimension(400,300);
        frame.setSize(size);
        //计算位置

        frame.setLocation(UIUtil.calcuCenterPoint(400,300));
        //添加关闭事件
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        GridLayout layout = new GridLayout(1,2);
        //添加组件
        JPanel panel = new JPanel();
        panel.setLayout(null);

//        panel.setSize(500,400);
        //label
        JLabel label = new JLabel("包名:");
        label.setBounds(10,10,40,30);
        JTextField packageField = new JTextField();
        packageField.setBounds(55,10,310,30);
        //设置默认值
        if (project != null) {
            packageField.setText(project.getName());
        }
        label.setLabelFor(packageField);


        panel.add(label);
        panel.add(packageField);

        JLabel message = new JLabel("");
        message.setBounds(10,70,300,100);
        message.setForeground(Color.red);
        panel.add(message);
        frame.add(panel);
        //添加按钮

        InstallButton submit = new InstallButton(false);
        submit.setText("初始化");
        submit.setBackground(Color.gray);
        submit.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Pattern pattern = Pattern.compile("^([a-z]+\\.)*[a-z]+$");
                if (!pattern.matcher(packageField.getText()).find()) {
                    message.setText("包名非法");
                    return;
                }
                message.setText("");
                // 尝试处理
                if (project == null) {
                    message.setText("获取项目信息失败！");
                    return;
                }
                try {
                    String res = new TemplateBuilder().initProjectFile(project,packageField.getText());
                    if (!"success".equalsIgnoreCase(res)) {
                        message.setText(res);
                        return;
                    }
                    frame.setVisible(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    message.setText("初始化失败：" + ex.getMessage());
                }
            }
        });
        submit.setBounds(290,40,90,25);
        panel.add(submit);
    }

    public void show(){
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        new InitProjectForm(null).show();
    }


}
