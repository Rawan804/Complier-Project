package semantic;

public class SemanticError {

    public enum Severity { ERROR, WARNING }

    private final Severity severity;
    private final String  message;
    private final int     line;
    private final String  source;   // "Python" | "HTML" | "CSS"

    public SemanticError(Severity severity, String message, int line, String source) {
        this.severity = severity;
        this.message  = message;
        this.line     = line;
        this.source   = source;
    }

    public Severity getSeverity() { return severity; }
    public String   getMessage()  { return message;  }
    public int      getLine()     { return line;     }
    public String   getSource()   { return source;   }

    @Override
    public String toString() {
        return String.format("[%s] [%s - line %d] %s", severity, source, line, message);
    }
}
