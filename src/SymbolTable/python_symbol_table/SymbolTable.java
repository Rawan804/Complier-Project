package SymbolTable.python_symbol_table;

import java.util.*;

public class SymbolTable {

    // كل الـ scopes المفتوحة حالياً (stack)
    private final List<Map<String, SymbolRow>> scopes = new ArrayList<>();

    // كل الـ scopes اللي فُتحت على مر التاريخ (للطباعة)
    private final List<ScopeRecord> allScopes = new ArrayList<>();

    // عداد لترقيم الـ scopes
    private int scopeCounter = 0;

    public SymbolTable() {
        // نفتح الـ global scope مرة وحدة فقط هون
        allocate("global");
    }

    /**
     * يفتح scope جديد ويعطيه label وصفي
     */
    public void allocate(String label) {
        Map<String, SymbolRow> newScope = new LinkedHashMap<>();
        scopes.add(newScope);
        allScopes.add(new ScopeRecord(scopeCounter++, label, newScope));
    }

    /**
     * يفتح scope بدون label (للتوافق مع الكود القديم)
     */
    public void allocate() {
        allocate("block");
    }

    /**
     * يغلق الـ scope الحالي (يطلعه من الـ stack بس يبقى في allScopes)
     */
    public void free() {
        if (!scopes.isEmpty())
            scopes.remove(scopes.size() - 1);
    }

    /**
     * يضيف رمز في الـ scope الحالي
     * إذا الاسم موجود مسبقاً بنرجع نفس الـ row بدل ما نضيف واحد جديد
     */
    public SymbolRow insert(String name) {
        Map<String, SymbolRow> currentScope = scopes.get(scopes.size() - 1);
        if (currentScope.containsKey(name)) {
            return currentScope.get(name); // نرجع الموجود بدل إنشاء جديد
        }
        SymbolRow row = new SymbolRow(name);
        currentScope.put(name, row);
        return row;
    }

    /**
     * يبحث من الـ scope الحالي للـ outer — scoped lookup
     */
    public SymbolRow lookup(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name))
                return scopes.get(i).get(name);
        }
        return null;
    }

    /**
     * يبحث في الـ scope الحالي فقط (للكشف عن redeclaration)
     */
    public SymbolRow lookupCurrentScope(String name) {
        if (scopes.isEmpty()) return null;
        return scopes.get(scopes.size() - 1).get(name);
    }

    /**
     * يبحث في الـ global scope فقط
     */
    public SymbolRow lookupGlobal(String name) {
        if (allScopes.isEmpty()) return null;
        return allScopes.get(0).scope.get(name);
    }

    /**
     * يضيف usedAt لأي رمز موجود — helper مركزي
     */
    public void recordUsage(String name, int line) {
        SymbolRow row = lookup(name);
        if (row == null) return;
        @SuppressWarnings("unchecked")
        List<Integer> usages = (List<Integer>) row.getAttributes().get("usedAt");
        if (usages == null) {
            usages = new ArrayList<>();
            row.setAttribute("usedAt", usages);
        }
        if (!usages.contains(line)) {
            usages.add(line);
        }
    }

    /**
     * طباعة جميل للجدول — يطبع فقط الـ scopes الغير فاضية
     */
    public void printTable() {
        System.out.println("===== SYMBOL TABLE =====");
        for (ScopeRecord record : allScopes) {
            System.out.println("Scope #" + record.id + " [" + record.label + "]:");
            if (record.scope.isEmpty()) {
                System.out.println("  <empty>");
            } else {
                System.out.printf("  %-20s | %-12s | %-4s | %s%n",
                        "Name", "Type", "Line", "Attributes");
                System.out.println("  " + "-".repeat(70));
                for (SymbolRow row : record.scope.values()) {
                    System.out.printf("  %-20s | %-12s | %-4d | %s%n",
                            row.getName(), row.getType(), row.getLine(), row.getAttributes());
                }
            }
            System.out.println();
        }
        System.out.println("========================");
    }

    /**
     * record داخلي لحفظ الـ scope مع الـ label والـ id
     */
    public static class ScopeRecord {
        final int id;
        final String label;
        final Map<String, SymbolRow> scope;

        ScopeRecord(int id, String label, Map<String, SymbolRow> scope) {
            this.id = id;
            this.label = label;
            this.scope = scope;
        }
    }
    // في SymbolTable.java — أضف:
    public SymbolRow lookupInAllScopes(String name) {
        for (ScopeRecord record : allScopes) {
            if (record.scope.containsKey(name))
                return record.scope.get(name);
        }
        return null;
    }
    /**
     * تفحص ما إذا كان الرمز موجوداً في أي سكوب تم إنشاؤه في البرنامج بأكمله
     * (تُستخدم لتمييز الـ Scope Error عن الـ Not Defined)
     */
    public boolean existsAnywhereInProgram(String name) {
        for (ScopeRecord record : allScopes) {
            if (record.scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * يعيد إدخال سكوب تم إنشاؤه سابقاً إلى الـ Stack الحية (إعادة تفعيل)
     */
    public void reActivateScope(String label) {
        for (ScopeRecord record : allScopes) {
            if (record.label.equals(label)) {
                scopes.add(record.scope);
                return;
            }
        }
    }

    /**
     * يزيل السكوب الحالي من الـ Stack الحية فقط دون حذفه من التاريخ
     */
    public void deactivateCurrentScope() {
        if (!scopes.isEmpty()) {
            scopes.remove(scopes.size() - 1);
        }
    }
    // وخلّي ScopeRecord public أو أضف getter:
    public List<ScopeRecord> getAllScopes() { return allScopes; }
}