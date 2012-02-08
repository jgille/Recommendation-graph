package recng.common.io;

/**
 * Settings class used when working with csv files.
 *
 * @author jon
 *
 */
public class CSVDialect {

    public static final char DEFAULT_SEPARATOR = ';';
    public static final char DEFAULT_ESCAPE_CHAR = '\\';
    public static final char DEFAULT_QUOTE_CHAR = '"';

    private char separator = DEFAULT_SEPARATOR;
    private char escapeChar = DEFAULT_ESCAPE_CHAR;
    private char quoteChar = '"';

    public char getSeparator() {
        return separator;
    }

    public CSVDialect setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public CSVDialect setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public CSVDialect setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }
}
