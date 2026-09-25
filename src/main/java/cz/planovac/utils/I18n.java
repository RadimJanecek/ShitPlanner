package cz.planovac.utils;

import java.util.HashMap;
import java.util.Map;

public class I18n {
    public enum Language { CS, EN }
    private static Language currentLanguage = Language.CS;
    private static final Map<String, String> enDict = new HashMap<>();

    static {
        // Hlavní okno
        enDict.put("Plánovač směn", "Shift Planner");
        enDict.put("Možnosti", "Options");
        enDict.put("⚙️ Nastavení aplikace", "⚙️ App Settings");
        enDict.put("Pravidla", "Rules");
        enDict.put("Typy směn", "Shift Types");
        enDict.put("Zakázané páry", "Forbidden Pairs");
        enDict.put("Požadavky na obsazení směn", "Coverage Requirements");
        enDict.put("Koncepty", "Drafts");
        enDict.put("📂 Zobrazit koncepty", "📂 View Drafts");
        enDict.put("Historie", "History");
        enDict.put("📅 Zobrazit historii", "📅 View History");
        enDict.put("Přidat zaměstnance", "Add Employee");
        enDict.put("Jm:", "Name:");
        enDict.put("Příj:", "Surname:");
        enDict.put("Úv:", "FTE:");
        enDict.put("Přidat", "Add");
        enDict.put("Seznam zaměstnanců", "Employee List");
        enDict.put("Smazat vybraného zaměstnance", "Delete Selected Employee");
        enDict.put("Nastavení plánování", "Planning Settings");
        enDict.put("Od:", "From:");
        enDict.put(" Do:", " To:");
        enDict.put("Nastavené absence", "Set Absences");
        enDict.put("Přidat absenci", "Add Absence");
        enDict.put("Smazat absence", "Delete Absence");
        enDict.put("📊 Počítám kapacitu...", "📊 Calculating capacity...");
        enDict.put("⚙️ Vytvořit nový plán", "⚙️ Create New Plan");
        enDict.put("Úvazek", "FTE");
        
        // Směny (ZÁKLADNÍ PŘEKLAD)
        enDict.put("Ranní", "Morning");
        enDict.put("Odpolední", "Afternoon");
        enDict.put("Noční", "Night");
        
        // Vyskakovací okna (Dialogy)
        enDict.put("Nastavení aplikace", "App Settings");
        enDict.put("Brigádník (úvazek < 1.0) nesmí být na směně sám", "Part-timer (FTE < 1.0) cannot be alone on shift");
        enDict.put("Zobrazovat pouze zkratky směn (první písmeno, ...)", "Show only shift abbreviations (first letter)");
        enDict.put("Minimální odpočinek mezi směnami (hodiny):", "Minimum rest between shifts (hours):");
        enDict.put("Jazyk / Language:", "Language:");
        enDict.put("Uložit nastavení", "Save Settings");
        enDict.put("Zrušit", "Cancel");
        
        enDict.put("Spravovat typy směn", "Manage Shift Types");
        enDict.put("Název (např. Ranní):", "Name (e.g., Morning):");
        enDict.put("Začátek (HH:MM):", "Start (HH:MM):");
        enDict.put("Konec (HH:MM):", "End (HH:MM):");
        enDict.put("Smazat směnu", "Delete Shift");
        
        enDict.put("Spravovat zakázané páry", "Manage Forbidden Pairs");
        enDict.put("Přidat nový zakázaný pár", "Add New Forbidden Pair");
        enDict.put("Zaměstnanec 1:", "Employee 1:");
        enDict.put("Zaměstnanec 2:", "Employee 2:");
        enDict.put("Přidat pár", "Add Pair");
        enDict.put("Existující zakázané páry", "Existing Forbidden Pairs");
        enDict.put("Smazat vybraný pár", "Delete Selected Pair");
        enDict.put("Zavřít", "Close");
        enDict.put(" a ", " and ");
        
        enDict.put("Upravit požadavky na obsazení směn", "Edit Coverage Requirements");
        enDict.put("Typ směny", "Shift Type");
        enDict.put("Počet zaměstnanců", "Employee Count");
        
        enDict.put("Zobrazit koncepty", "View Drafts");
        enDict.put("Rozpracované koncepty", "Drafts in Progress");
        enDict.put("Otevřít", "Open");
        enDict.put("Smazat", "Delete");
        enDict.put("Zobrazit historii", "View History");
        enDict.put("Schválená historie (Měsíce)", "Approved History (Months)");
        enDict.put("Otevřít historii", "Open History");
        enDict.put("Smazat měsíc", "Delete Month");
        enDict.put("Nejprve vyberte položku ze seznamu.", "First, select an item from the list.");
        enDict.put("Opravdu smazat koncept: ", "Really delete draft: ");
        enDict.put("Potvrzení", "Confirmation");
        enDict.put("Opravdu trvale smazat historii pro měsíc ", "Really permanently delete history for month ");

        // Tabulky plánů
        enDict.put("Plán směn", "Shift Plan");
        enDict.put("Zaměstnanec", "Employee");
        enDict.put("Odpracováno", "Worked");
        enDict.put("Exporty", "Exports");
        enDict.put("Excel (.csv)", "Excel (.csv)");
        enDict.put("PDF", "PDF");
        enDict.put("Otevře systémový dialog tisku. Zvolte 'Microsoft Print to PDF' pro uložení souboru.", "Opens print dialog. Select 'Microsoft Print to PDF' to save.");
        enDict.put("1. Přizpůsobit na 1 stranu (pro 1-2 týdny)", "1. Fit to 1 page (for 1-2 weeks)");
        enDict.put("2. Rozdělit na více stran (pro měsíce/roky!)", "2. Split to multiple pages (for months/years!)");
        enDict.put("Jak chcete tabulku vytisknout / uložit do PDF?", "How do you want to print / save to PDF?");
        enDict.put("Nastavení tisku", "Print Settings");
        enDict.put("Strana", "Page");
        enDict.put("Uložit koncept", "Save Draft");
        enDict.put("Zapsat do historie", "Save to History");
        enDict.put("Zavřít historii", "Close History");
        enDict.put("Zadejte název:", "Enter name:");
        enDict.put("Uložit", "Save");
        enDict.put("Schválení plánu", "Plan Approval");
        enDict.put("Zapsat jako finální? Tento krok plán vloží do historie.", "Save as final? This will add the plan to history.");
        enDict.put("Koncept s názvem '", "Draft named '");
        enDict.put("' již existuje.\nChcete jej přepsat?", "' already exists.\nDo you want to overwrite it?");
        enDict.put("Přepsat koncept?", "Overwrite draft?");
        enDict.put("Chyba při exportu/tisku: ", "Export/Print error: ");
        enDict.put("Chyba tisku: ", "Print error: ");
        enDict.put("Plán byl úspěšně exportován!", "Plan successfully exported!");
        enDict.put("Historie úspěšně exportována!", "History successfully exported!");
        enDict.put("Úspěch", "Success");
        enDict.put("⚠️ Zaměstnanec má v tento den nastavenou absenci!\n", "⚠️ Employee has an absence on this day!\n");
        enDict.put("⚠️ Porušení odpočinku!\n", "⚠️ Rest period violation!\n");
        enDict.put("Pokračovat?", "Continue?");
        enDict.put("Porušení pravidel", "Rule Violation");
        enDict.put("📅 Prohlížet historii (Schválené plány)", "📅 View History (Approved)");
    }

    public static void setLanguage(Language lang) { currentLanguage = lang; }
    public static Language getLanguage() { return currentLanguage; }

    public static String t(String text) {
        if (currentLanguage == Language.EN && enDict.containsKey(text)) {
            return enDict.get(text);
        }
        return text;
    }
}