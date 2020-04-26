package ir.ac.um.melogger.analysis.instrument;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import ir.ac.um.melogger.analysis.utils.AnalysisUtils;
import ir.ac.um.melogger.utils.Utils;

public class ContextInjectorThread implements Runnable {
    private Instrumenter instrumenter;
    private PsiClass aClass;

    public ContextInjectorThread(Instrumenter instrumenter, PsiClass aClass) {
        this.instrumenter = instrumenter;
        this.aClass = aClass;
    }

    @Override
    public void run() {
        WriteCommandAction.runWriteCommandAction(instrumenter.getProject(), () -> {
            boolean injected = false;
            if (!isAlreadyInjected(aClass, AnalysisUtils.CONTEXT_GETTER_METHOD_NAME)) {
                Utils.showMessage("\tApplication Class Found: " + aClass.getQualifiedName());
                String statementText = AnalysisUtils.CONTEXT_GETTER_METHOD_BODY;
                final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(instrumenter.getProject());
                final PsiElementFactory factory = psiFacade.getElementFactory();
                PsiMethod method = factory.createMethodFromText(statementText, aClass);
                final PsiElement addedElement = aClass.add(method);
                final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(instrumenter.getProject());
                codeStyleManager.reformat(addedElement);
                injected = true;
            }
            instrumenter.contextInjected(aClass.getQualifiedName(), injected);
        });
    }

    private boolean isAlreadyInjected(PsiClass aClass, String methodName) {
        boolean defined = false;
        PsiMethod[] methods = aClass.getMethods();
        for (PsiMethod method : methods) {
            if (method.getName().equals(methodName)) {
                defined = true;
                break;
            }
        }
        return defined;
    }
}
