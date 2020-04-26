package ir.ac.um.melogger.analysis;

import ir.ac.um.melogger.utils.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

public class ManifestAnalyzer {
    public String findApplicationClass(File manifest) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            ManifestHandler handler = new ManifestHandler();
            saxParser.parse(manifest, handler);
            String packageName = handler.getPackageName();
            String applicationName = handler.getApplicationName();
            if (applicationName.startsWith(".")) {
                applicationName = packageName + applicationName;
            }
            return applicationName;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Utils.showException(e);
        }
        return null;
    }
}

class ManifestHandler extends DefaultHandler {
    private String applicationName;
    private String packageName;

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("manifest")) {
            packageName = atts.getValue("package");
        } else if (localName.equals("application")) {
            applicationName = atts.getValue("android:name");
        }
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getPackageName() {
        return packageName;
    }

}

