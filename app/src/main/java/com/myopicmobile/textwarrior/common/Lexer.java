package com.myopicmobile.textwarrior.common;

import android.graphics.Rect;
import android.util.Log;

import com.androlua.LuaLexer;
import com.androlua.LuaTokenTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    public final static int UNKNOWN = -1; //未知
    public final static int NORMAL = 0; //通用
    public final static int KEYWORD = 1;//关键字
    public final static int OPERATOR = 2;//一元操作符
    public final static int TOPERATOR = 3;//二元操作符
    public final static int NAME = 4; //变量名
    public final static int FUNC = 5; //函数名
    public final static int REQUIRE = 6; //导入包
    public final static int NUMBER = 7; //数字
    public final static int BASE = 8; //基础库
    public final static int KEY = 9; //键值
    public final static int STRING = 10; //字符串
    public final static int LONGSTRING = 11;
    public final static int BOOL = 12;
    public final static int NIL = 13;
    public final static int EXIT = 14;
    public final static int COMMENT = 30; //注释

    private static Language _globalLanguage = LanguageNonProg.getInstance();
    LexCallback _callback = null;
    private DocumentProvider _hDoc;
    private LexThread _workerThread = null;

    public Lexer(LexCallback callback) {
        _callback = callback;
    }

    synchronized public static Language getLanguage() {
        return _globalLanguage;
    }

    synchronized public static void setLanguage(Language lang) {
        _globalLanguage = lang;
    }

    private static boolean isKeyword(LuaTokenTypes t) {
        switch (t) {
            case TRUE:
            case FALSE:
            case DO:
            case FUNCTION:
            case NOT:
            case AND:
            case OR:
            case IF:
            case THEN:
            case ELSEIF:
            case ELSE:
            case WHILE:
            case FOR:
            case IN:
            case RETURN:
            case BREAK:
            case LOCAL:
            case REPEAT:
            case UNTIL:
            case END:
            case NIL:
            case SWITCH:
            case CASE:
            case DEFAULT:
            case CONTINUE:
            case GOTO:
            case LAMBDA:
            case DEFER:
            case WHEN:
                return true;
            default:
                return false;
        }
    }

    public void tokenize(DocumentProvider hDoc) {
        if (!Lexer.getLanguage().isProgLang()) {
            return;
        }

        //tokenize will modify the state of hDoc; make a copy
        setDocument(new DocumentProvider(hDoc));
        if (_workerThread == null) {
            _workerThread = new LexThread(this);
            _workerThread.start();
        } else {
            _workerThread.restart();
        }
    }

    void tokenizeDone(List<Pair> result) {
        if (_callback != null) {
            _callback.lexDone(result);
        }
        _workerThread = null;
    }

    public void cancelTokenize() {
        if (_workerThread != null) {
            _workerThread.abort();
            _workerThread = null;
        }
    }

    public synchronized DocumentProvider getDocument() {
        return _hDoc;
    }

    public synchronized void setDocument(DocumentProvider hDoc) {
        _hDoc = hDoc;
    }

    public interface LexCallback {
        public void lexDone(List<Pair> results);
    }

    private static ArrayList<Rect> mLines = new ArrayList<>();

    public static ArrayList<Rect> getLines() {
        return mLines;
    }

    private class LexThread extends Thread {
        private final Lexer _lexManager;
        /**
         * can be set by another thread to stop the scan immediately
         */
        private final Flag _abort;
        private boolean rescan = false;
        private int max = 2 ^ 18;
        /**
         * A collection of Pairs, where Pair.first is the start
         * position of the token, and Pair.second is the type of the token.
         */
        private ArrayList<Pair> _tokens;

        public LexThread(Lexer p) {
            _lexManager = p;
            _abort = new Flag();
        }

        @Override
        public void run() {
            do {
                rescan = false;
                _abort.clear();
                if (Lexer.getLanguage() instanceof LanguageLua) {
                    tokenize();
                }
            }
            while (rescan);

            if (!_abort.isSet()) {
                // lex complete
                _lexManager.tokenizeDone(_tokens);
            }
        }

        public void restart() {
            rescan = true;
            _abort.set();
        }

        public void abort() {
            _abort.set();
        }


        private void tokenize() {
            DocumentProvider hDoc = getDocument();
            int rowCount = hDoc.getRowCount();
            int maxRow = 9999;
            ArrayList<Pair> tokens = new ArrayList<Pair>(8196);
            ArrayList<Rect> lines = new ArrayList<>(8196);
            ArrayList<Rect> lineStacks = new ArrayList<>(8196);
            ArrayList<Rect> lineStacks2 = new ArrayList<>(8196);

            LuaLexer lexer = new LuaLexer(hDoc);
            Language language = Lexer.getLanguage();
            language.clearUserWord();
            try {
                int idx = 0;

                LuaTokenTypes lastType = null;
                LuaTokenTypes lastType2 = null;
                LuaTokenTypes lastType3 = null;

                String lastName = "";
                Pair lastPair = null;
                int lastLen = 0;
                StringBuilder bul = new StringBuilder();
                StringBuilder bul2 = new StringBuilder();
                boolean isModule = false;
                boolean isClass = false;
                boolean hasDo = true;
                int lastNameIdx = -1;
                while (!_abort.isSet()) {
                    Pair pair = null;
                    LuaTokenTypes type = lexer.advance();
                    if (type == null)
                        break;
                    int len = lexer.yylength();

                    if (isModule && lastType == LuaTokenTypes.STRING && type != LuaTokenTypes.STRING) {
                        String mod = bul.toString();
                        if (bul.length() > 2) {
                            language.addUserWord(mod.substring(1, mod.length() - 1), NAME);
                        }
                        bul = new StringBuilder();
                        isModule = false;
                    }
                    else if (isClass && lastType == LuaTokenTypes.STRING && type != LuaTokenTypes.STRING) {
                        String mod = bul2.toString();
                        if (bul2.length() > 2) {
                            setClassName(mod.substring(1, mod.length() - 1));
                        }
                        bul2 = new StringBuilder();
                        isClass = false;
                    }
                    /*if (lastType2 == type && lastPair != null) {
                        lastPair.setFirst(lastLen += len);
                        continue;
                    }*/
                    lastLen = len;
                    switch (type) {
                        case DO:
                            if (hasDo) {
                                lineStacks.add(new Rect(lexer.yychar(), lexer.yyline(), 0, lexer.yyline()));
                            }
                            hasDo = true;
                            //关键字
                            tokens.add(new Pair(len, KEYWORD));
                            break;
                        case WHILE:
                        case FOR:
                            hasDo = false;
                            lineStacks.add(new Rect(lexer.yychar(), lexer.yyline(), 0, lexer.yyline()));
                            //关键字
                            tokens.add(new Pair(len, KEYWORD));
                            break;
                        case FUNCTION:
                        case IF:
                        case SWITCH:
                            lineStacks.add(new Rect(lexer.yychar(), lexer.yyline(), 0, lexer.yyline()));
                            //关键字
                            tokens.add(new Pair(len, KEYWORD));
                            break;
                        case END:
                            int size = lineStacks.size();
                            if (size > 0) {
                                Rect rect = lineStacks.remove(size - 1);
                                rect.bottom = lexer.yyline();
                                rect.right = lexer.yychar();
                                if (rect.bottom - rect.top>1)
                                    lines.add(rect);
                            }
                            //关键字
                            tokens.add(new Pair(len, KEYWORD));
                            hasDo = true;
                            break;
                        case TRUE:
                        case FALSE:
                            tokens.add(new Pair(len, BOOL));
                            hasDo = true;
                            break;
                        case NIL:
                            //关键字
                            tokens.add(new Pair(len, NIL));
                            break;
                        case NOT:
                        case AND:
                        case OR:
                        case THEN:
                        case ELSEIF:
                        case ELSE:
                        case IN:
                        case RETURN:
                        case BREAK:
                        case LOCAL:
                        case REPEAT:
                        case UNTIL:
                        case CASE:
                        case DEFAULT:
                        case CONTINUE:
                        case GOTO:
                        case LAMBDA:
                        case WHEN:
                        case DEFER:
                            //关键字
                            tokens.add(new Pair(len, KEYWORD));
                            break;
                        case LCURLY:
                            lineStacks2.add(new Rect(lexer.yychar(), lexer.yyline(), 0, lexer.yyline()));
                            //符号
                            tokens.add(pair = new Pair(len, OPERATOR));
                            break;
                        case RCURLY:
                            int size2 = lineStacks2.size();
                            if (size2 > 0) {
                                Rect rect = lineStacks2.remove(size2 - 1);
                                rect.bottom = lexer.yyline();
                                rect.right = lexer.yychar();
                                if (rect.bottom - rect.top>1)
                                    lines.add(rect);
                            }
                            //符号
                            tokens.add(pair = new Pair(len, OPERATOR));
                            break;
                        case LPAREN: //括号
                            //符号
                            if(lastType == LuaTokenTypes.NAME){
                                Pair p = tokens.get(tokens.size()-1);
                                if(p.getSecond()==NORMAL) {
                                    p.setSecond(FUNC);
                                }
                            }
                            tokens.add(pair = new Pair(len, OPERATOR));
                            break;
                        case RPAREN:
                        case LBRACK: //中括号
                        case RBRACK:
                        case COMMA: //逗号
                        case DOT: //点
                            //符号
                            tokens.add(pair = new Pair(len, OPERATOR));
                            break;
                        case STRING:
                            //字符串
                            tokens.add(pair = new Pair(len, STRING));
                            if (rowCount > maxRow)
                                break;

                            if (lastName.equals("require")) {
                                isModule = true;
                            }else if(lastName.equals("import")){
                                isClass = true;
                            }
                            if (isModule) {
                                bul.append(lexer.yytext());
                            }
                            if(isClass){
                                bul2.append(lexer.yytext());
                            }
                            break;
                        case LONG_STRING:
                            //字符串
                            tokens.add(pair = new Pair(len, LONGSTRING));
                            if (rowCount > maxRow)
                                break;

                            if (lastName.equals("require")) {
                                isModule = true;
                            }else if(lastName.equals("import")){
                                isClass = true;
                            }
                            if (isModule) {
                                bul.append(lexer.yytext());
                            }
                            if(isClass){
                                bul2.append(lexer.yytext());
                            }
                            break;
                        case NAME:
                            if (rowCount > maxRow) {
                                tokens.add(new Pair(len, NORMAL));
                                break;
                            }
                            if (lastType2 == LuaTokenTypes.NUMBER) {
                                Pair p = tokens.get(tokens.size()-1);
                                p.setSecond(NUMBER);
                                p.setFirst(p.getFirst()+len);
                            }
                            String name = lexer.yytext();
                            if (lastType == LuaTokenTypes.FUNCTION) {
                                //函数名
                                tokens.add(new Pair(len, FUNC));
                                language.addUserWord(name, FUNC);
                            }else if(name.equals("exit")){
                                tokens.add(new Pair(len, EXIT));
                            } else if (lastType == LuaTokenTypes.GOTO || lastType == LuaTokenTypes.AT) {
                                tokens.add(new Pair(len, KEYWORD));
                            } else if (lastType == LuaTokenTypes.MULT && lastType3 == LuaTokenTypes.LOCAL) {
                                tokens.add(new Pair(len, OPERATOR));
                            } else if (language.isBasePackage(name)) {
                                tokens.add(new Pair(len, BASE));
                            } else if (lastType == LuaTokenTypes.DOT && language.isBasePackage(lastName) && language.isBaseWord(lastName, name)) {
                                //标准库函数
                                tokens.add(new Pair(len, BASE));
                            } else if (lastType == LuaTokenTypes.DOT && (!language.isBasePackage(lastName))) {
                                //KEY
                                tokens.add(new Pair(len, KEY));
                            } else if (language.isName(name)) {
                                tokens.add(new Pair(len, BASE));
                            } else {
                                tokens.add(new Pair(len, NORMAL));
                                language.addUserWord(name,NORMAL);
                            }

                            if (lastType == LuaTokenTypes.ASSIGN && name.equals("require")) {
                                language.addUserWord(lastName,REQUIRE);
                                if (lastNameIdx>=0) {
                                    Pair p = tokens.get(lastNameIdx-1);
                                    p.setSecond(REQUIRE);
                                    lastNameIdx=-1;
                                }
                            }

                            lastNameIdx=tokens.size();
                            lastName = name;
                            break;
                        case SHORT_COMMENT:
                        case BLOCK_COMMENT:
                        case DOC_COMMENT:
                            //注释
                            tokens.add(pair = new Pair(len, COMMENT));
                            break;
                        case NUMBER:
                            //数字
                            tokens.add(new Pair(len, NUMBER));
                            language.addUserWord(lexer.yytext(),NUMBER);
                            break;
                        default:
                            tokens.add(pair = new Pair(len, NORMAL));
                    }
                    lastType3=lastType;
                    if (type != LuaTokenTypes.WHITE_SPACE
                        //&& type != LuaTokenTypes.NEWLINE && type != LuaTokenTypes.NL_BEFORE_LONGSTRING
                    ) {
                        lastType = type;
                    }
                    lastType2 = type;
                    if (pair != null)
                        lastPair = pair;
                    idx += len;
                }
            } catch (Exception e) {
                e.printStackTrace();
                TextWarriorException.fail(e.getMessage());
            }
            if (tokens.isEmpty()) {
                // return value cannot be empty
                tokens.add(new Pair(0, NORMAL));
            }
            language.updateUserWord();
            mLines = lines;
            _tokens = tokens;
        }
    }//end inner class

    public void setClassName(String cls){
        try {
            Class<?> c = Class.forName(cls);
            List<Field> fieldList = new ArrayList<>();
            List<Method> methodList = new ArrayList<>(new ArrayList<>(Arrays.asList(c.getDeclaredMethods())));
            while (c != null) {
                fieldList.addAll(new ArrayList<>(Arrays.asList(c.getDeclaredFields())));
                c = c.getSuperclass();
            }
            Field[] fields = new Field[fieldList.size()];
            Method[] methods = new Method[methodList.size()];
            fieldList.toArray(fields);
            methodList.toArray(methods);
            String[] llst = new String[fields.length + methods.length];
            int l = 0;
            for (Method m : methods) {
                llst[l] = m.getName();
                l = l + 1;
            }
            for (Field f : fields) {
                llst[l] = f.getName();
                l = l + 1;
            }
            try {
                String name = c.getName();
                LanguageLua.classfunc.put(name,llst);
            }catch (NullPointerException er){
                er.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}