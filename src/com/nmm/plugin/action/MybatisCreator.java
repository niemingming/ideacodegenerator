package com.nmm.plugin.action;

import com.intellij.database.psi.DbTable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import com.nmm.plugin.action.ui.DataSourcePasswordUI;

public class MybatisCreator extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        int i = 0;
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if (element instanceof DbTable ) {
            // 获取原配置名称
            DbTable table = (DbTable) element;
            String sourceName = table.getDataSource().getName();
            table.getDasParent();
            new DataSourcePasswordUI(e.getProject(),sourceName,table).show();
        }
    }



}
