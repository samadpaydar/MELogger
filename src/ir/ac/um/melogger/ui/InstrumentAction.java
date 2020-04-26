package ir.ac.um.melogger.ui;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.ui.content.Content;
import ir.ac.um.melogger.utils.Constants;
import ir.ac.um.melogger.utils.Utils;
import org.jetbrains.annotations.NotNull;
import ir.ac.um.melogger.analysis.instrument.Instrumenter;

/**
 * @author Samad Paydar
 */
public class InstrumentAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent != null) {
            Navigatable navigatable = anActionEvent.getData(CommonDataKeys.NAVIGATABLE);
            if (navigatable != null) {
                Project project = anActionEvent.getProject();
                PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
                processProject(project, psiElement);
            }
        }
    }

    private void processProject(Project project, PsiElement psiElement) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.PLUGIN_NAME);
        ConsoleView consoleView = Utils.getConsoleView();
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Utils.setConsoleView(consoleView);
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), Constants.PLUGIN_NAME, true);
            toolWindow.getContentManager().addContent(content);
        }
        toolWindow.show(null);
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Instrumenter(project, psiElement));
            }
        });

    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        boolean enabled = project != null && (psiElement instanceof PsiJavaDirectoryImpl)
                && ((PsiDirectory) psiElement).getVirtualFile().getCanonicalPath().equals(
                        project.getBasePath());
        anActionEvent.getPresentation().setEnabledAndVisible(enabled);
    }

}
