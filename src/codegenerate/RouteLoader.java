package codegenerate;

import codegenerate.GenerateCode.RenderTemplateCallFinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteLoader {

    public static List<JinjaGenerator.RouteInfo> loadRoutes(
            List<RenderTemplateCallFinder.RenderTemplateCall> renderCalls,
            Map<String, Object> contextData,
            Path templatesDir
    ) {
        List<JinjaGenerator.RouteInfo> routes = new ArrayList<>();

        for (RenderTemplateCallFinder.RenderTemplateCall call : renderCalls) {
            Map<String, String> resolvedTypes = new HashMap<>();
            Map<String, Object> resolvedValues = new HashMap<>();

            for (Map.Entry<String, String> entry : call.contextVars.entrySet()) {
                String templateVarName = entry.getKey();
                String pythonVarName = entry.getValue();

                String type = "unknown";
                Object value = null;

                if (contextData.containsKey(pythonVarName)) {
                    value = contextData.get(pythonVarName);
                    type = inferType(value);
                }

                resolvedTypes.put(templateVarName, type);
                resolvedValues.put(templateVarName, value);
            }

            String jinjaFile = resolveJinjaFile(call.templateFile, templatesDir);

            routes.add(new JinjaGenerator.RouteInfo(
                    call.routeName,
                    jinjaFile,
                    call.templateFile,
                    resolvedTypes,
                    resolvedValues
            ));
        }

        return routes;
    }

    private static String inferType(Object value) {
        if (value instanceof List) return "collection";
        if (value instanceof Map) return "object";
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        return "string";
    }

    private static String resolveJinjaFile(String templateName, Path templatesDir) {
        String baseName = templateName;
        if (baseName.endsWith(".html")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }

        String jinjaName = baseName + ".jinja";
        Path jinjaPath = templatesDir.resolve(jinjaName);

        if (Files.exists(jinjaPath)) {
            return jinjaName;
        }

        if (Files.exists(templatesDir.resolve(templateName))) {
            return templateName;
        }

        throw new RuntimeException(
                "No Jinja template found for '" + templateName
                        + "' (looked for " + jinjaPath + ")"
        );
    }
}
