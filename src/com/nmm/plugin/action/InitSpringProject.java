package com.nmm.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class InitSpringProject extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        new InitProjectForm(e.getProject()).show();
    }
}
