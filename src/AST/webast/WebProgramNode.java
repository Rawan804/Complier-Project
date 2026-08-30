package AST.webast;

import java.util.ArrayList;
import java.util.List;

public class WebProgramNode extends WebASTNode {

    private WebASTNode html;
    private WebASTNode css;

    public void setHtml(WebASTNode html) {
        this.html = html;
    }

    public void setCss(WebASTNode css) {
        this.css = css;
    }

    @Override
    public void print(int indent) {

        System.out.println("WEB PROGRAM");

        if (html != null)
            html.print(indent + 1);

        if (css != null)
            css.print(indent + 1);
    }
}