package ir.ac.um.melogger.analysis.uninstrument;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.FileAnalyzer;
import ir.ac.um.melogger.analysis.ManifestAnalyzer;
import ir.ac.um.melogger.analysis.instrument.ContextInjector;
import ir.ac.um.melogger.analysis.instrument.InvocationInjector;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;
import ir.ac.um.melogger.utils.Constants;
import ir.ac.um.melogger.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class UnInstrumenter implements Runnable {
    private Project project;
    private PsiElement psiElement;
    private List<PsiClass> projectJavaClasses;

    private File sourceDirectory;

    public UnInstrumenter(Project project, PsiElement psiElement) {
        this.project = project;
        this.psiElement = psiElement;
    }

    @Override
    public void run() {
        Utils.showMessage("Started processing project " + project.getName());
        removeInvocations();
        FileAnalyzer fileAnalyzer = new FileAnalyzer();
        this.sourceDirectory = fileAnalyzer.findSourceDirectory(new File(project.getBasePath()));
        if (this.sourceDirectory != null) {
            Utils.showMessage("Found src directory: " + this.sourceDirectory.getAbsolutePath());
            Utils.showMessage("Finding Android manifest file...");
            ManifestAnalyzer manifestAnalyzer = new ManifestAnalyzer();
            File manifest = fileAnalyzer.findManifestFile(this.sourceDirectory);
            if (manifest != null) {
                Utils.showMessage("Found Android manifest file: " + manifest.getAbsolutePath());
                Utils.showMessage("Finding application class name...");
                String applicationClassName = manifestAnalyzer.findApplicationClass(manifest);
                if (applicationClassName != null) {
                    Utils.showMessage("Found application class name: " + applicationClassName);
                    removeContext(applicationClassName);
                } else {
                    Utils.showMessage("Application class name is not found.");
                }
            } else {
                Utils.showMessage("Android manifest file is not found.");
            }
        } else {
            Utils.showMessage("src directory is not found.");
        }
    }

    private void removeInvocations() {
        Utils.showMessage("Removing invocations...");
        psiElement.accept(new InvocationRemover(this));
    }

    private void removeContext(String applicationClassName) {
        Utils.showMessage("Removing context...");
        psiElement.accept(new ContextRemover(this, applicationClassName));
    }

    void contextRemoved(String applicationClassName) {
        Utils.showMessage("Removing logger source...");
        removeLoggerSource(sourceDirectory, applicationClassName);
        Utils.showMessage("Finished");
    }

    private void removeLoggerSource(File sourceDirectory, String applicationClassQualifiedName) {
        File javaSourceDirectory = new File(sourceDirectory, "main" + File.separatorChar + "java");
        File loggerFileDirectory = new File(javaSourceDirectory, AnalysisUtils.LOGGER_PACKAGE.replace(Constants.DOT_CHAR, File.separatorChar));
        File loggerFile = new File(loggerFileDirectory, AnalysisUtils.LOGGER_CLASS);
        loggerFile.delete();
        loggerFileDirectory.delete();
    }

    public Project getProject() {
        return project;
    }

}

