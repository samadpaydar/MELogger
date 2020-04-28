package ir.ac.um.melogger.analysis.utils;

import com.intellij.psi.*;
import ir.ac.um.melogger.utils.Constants;
import ir.ac.um.melogger.utils.Utils;

/**
 * @author Samad Paydar
 */
public class AnalysisUtils {
    public static final String LOGGER_PACKAGE = "ir.ac.um.melogger";
    public static final String LOGGER_CLASS = "MELogger";
    public static final String LOGGER_CLASS_QUALIFIED_NAME = AnalysisUtils.LOGGER_PACKAGE + "." + AnalysisUtils.LOGGER_CLASS;
    public static final String LOG_STATEMENT_TEMPLATE = AnalysisUtils.LOGGER_PACKAGE + "."
            + AnalysisUtils.LOGGER_CLASS
            + ".log(\"%s\");";
    public static final String CONTEXT_GETTER_METHOD_NAME = "getAppContextForMELogger";

    public static final String CONTEXT_GETTER_METHOD_BODY =
            "public static Context " + AnalysisUtils.CONTEXT_GETTER_METHOD_NAME + "() {" +
                    "\t\treturn getApplicationContext();" +
                    "    }";
    public static final String LOGGER_SOURCE_CODE = String.format("package %s;" +
                    "import android.content.Context;" +
                    "import android.os.Environment;" +
                    "import java.io.File;" +
                    "import java.io.FileOutputStream;" +
                    "import java.io.PrintStream;" +
                    "import %s.%s;" +
                    "public class %s {" +
                    "    private static PrintStream logFile;" +
                    "    public static synchronized void log(String msg) {" +
                    "        try {" +
                    "            if (logFile == null) {" +
                    "                Context context = [application_class].%s();" +
                    "                File directory = context.getExternalCacheDir();" +
                    "                File file = new File (directory, \"ApplicationLog.log\");" +
                    "                if(file.exists()) {" +
                    "                    file.delete();" +
                    "                }" +
                    "                file.createNewFile();" +
                    "                logFile = new PrintStream(new FileOutputStream(file), true);" +
                    "            }" +
                    "            logFile.append(msg + \"\\n\");" +
                    "            logFile.flush();" +
                    "        } catch (Exception e) {" +
                    "            e.printStackTrace();" +
                    "        }" +
                    "    }" +
                    "}", AnalysisUtils.LOGGER_PACKAGE, AnalysisUtils.LOGGER_PACKAGE,
            AnalysisUtils.LOGGER_CLASS, AnalysisUtils.LOGGER_CLASS, AnalysisUtils.CONTEXT_GETTER_METHOD_NAME);

    public static String getMethodQualifiedName(PsiMethod method) {
        String result = null;
        try {
            String methodSemiQualifiedName = getMethodSemiQualifiedName(method);
            String className = method.getContainingClass().getQualifiedName();
            if (className == null) {
                return "AnonymousClass(" + methodSemiQualifiedName;
            }
            result = className + Constants.COMMA_CHAR + methodSemiQualifiedName;
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
        return result;
    }

    private static String prepareName(String name) {
        if (name != null) {
            name = name.replace(Constants.DOT_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.LEFT_BRACKET_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.RIGHT_BRACKET_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.LESS_THAN_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.GREATER_THAN_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.COMMA_CHAR, Constants.UNDERLINE_CHAR);
            name = name.replace(Constants.BLANK_SPACE_CHAR, Constants.UNDERLINE_CHAR);
        }
        return name;
    }


    private static String getMethodSemiQualifiedName(PsiMethod method) {
        StringBuilder methodNameBuilder = new StringBuilder();
        methodNameBuilder.append(method.getName());
        try {
            if (method.getParameters().length > 0) {
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    String parameterType = parameter.getTypeElement().getType().getCanonicalText();
                    parameterType = parameterType.substring(parameterType.lastIndexOf('.') + 1);
                    methodNameBuilder.append(Constants.UNDERLINE_CHAR).append(parameterType);
                }
            }
        } catch (Exception e) {
            Utils.showException(e);
            e.printStackTrace();
        }
        return methodNameBuilder.toString();
    }

    /**
     * determines whether the first statement of a method (a constructor) is
     * a call expression by this or super
     *
     * @param method a method, actually a constructor
     * @return true if the first statement of the body of the given method is a call
     * to this or super, false otherwise
     */
    public static boolean isFirstStatementThisOrSuper(PsiMethod method) {
        boolean result = false;
        if (method.getBody() != null && method.getBody().getStatements() != null) {
            PsiStatement firstStatement = method.getBody().getStatements()[0];
            if (firstStatement instanceof PsiExpressionStatement) {
                PsiExpressionStatement expressionStatement = (PsiExpressionStatement) firstStatement;
                PsiExpression expression = expressionStatement.getExpression();
                if (expression instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) expression;
                    String canonicalText = methodCallExpression.getMethodExpression().getCanonicalText();
                    result = canonicalText.equals("this") || canonicalText.equals("super");
                }
            }
        }
        return result;
    }


}


