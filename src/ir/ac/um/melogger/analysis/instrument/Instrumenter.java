package ir.ac.um.melogger.analysis.instrument;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import ir.ac.um.melogger.analysis.FileAnalyzer;
import ir.ac.um.melogger.analysis.ManifestAnalyzer;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;
import ir.ac.um.melogger.utils.Constants;
import ir.ac.um.melogger.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class Instrumenter implements Runnable {
    private Project project;
    private PsiElement psiElement;
    private File sourceDirectory;

    public Instrumenter(Project project, PsiElement psiElement) {
        this.project = project;
        this.psiElement = psiElement;
    }

    @Override
    public void run() {
        Utils.showMessage("Started processing project " + project.getName());
        Utils.showMessage("Instrumenting started.");
        injectInvocations();
        Utils.showMessage("Finding src directory...");
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
                    injectContext(applicationClassName);
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

    private void injectContext(String applicationClassName) {
        Utils.showMessage("Injecting context...");
        psiElement.accept(new ContextInjector(this, applicationClassName));
    }

    void contextInjected(String applicationClassName, boolean added) {
        if (!added) {
            Utils.showMessage("Context getter method is not injected, since it already exists.");
        }
        Utils.showMessage("Injecting logger source...");
        injectLoggerSource(sourceDirectory, applicationClassName);
        Utils.showMessage("Finished");
    }

    private void injectLoggerSource(File sourceDirectory, String applicationClassQualifiedName) {
        File javaSourceDirectory = new File(sourceDirectory, "main" + File.separatorChar + "java");
        File loggerFileDirectory = new File(javaSourceDirectory, AnalysisUtils.LOGGER_PACKAGE.replace(Constants.DOT_CHAR, File.separatorChar));
        boolean exists = loggerFileDirectory.exists();
        if (!exists) {
            boolean created = loggerFileDirectory.mkdirs();
            if (!created) {
                Utils.showMessage("Filed to create path " + loggerFileDirectory.getAbsolutePath());
            } else {
                exists = created;
            }
        }
        if (exists) {
            try (PrintStream javaFile = new PrintStream(new File(loggerFileDirectory, AnalysisUtils.LOGGER_CLASS))) {
                javaFile.println(AnalysisUtils.LOGGER_SOURCE_CODE.
                        replace("[application_class]", applicationClassQualifiedName));
            } catch (IOException e) {
                Utils.showException(e);
            }
        }
    }

    private void injectInvocations() {
        Utils.showMessage("Injecting invocations...");
        psiElement.accept(new InvocationInjector(this));
    }


    public Project getProject() {
        return project;
    }

}




