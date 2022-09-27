/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */

package com.myopicmobile.textwarrior.common;

import java.util.HashMap;

public abstract class ColorScheme
{
	public enum Colorable
	{
		FOREGROUND, BACKGROUND, SELECTION_FOREGROUND, SELECTION_BACKGROUND,
		CARET_FOREGROUND, CARET_BACKGROUND, CARET_DISABLED, LINE_HIGHLIGHT,
		NON_PRINTING_GLYPH, COMMENT, KEYWORD, NAME,STRING,BASE,
		SECONDARY,NUMBER,FUNC,KEY,LONGSTRING,BOOL,NIL,EXIT
	}

	protected HashMap<Colorable, Integer> _colors = generateDefaultColors();

	public void setColor(Colorable colorable, int color)
	{
		_colors.put(colorable, color);
	}

	public int getColor(Colorable colorable)
	{
		Integer color = _colors.get(colorable);
		if (color == null)
		{
			TextWarriorException.fail("Color not specified for " + colorable);
			return 0;
		}
		return color.intValue();
	}

	/**NORMAL = 0; //通用
	 KEYWORD = 1;//关键字
	 OPERATOR = 2;//一元操作符
	 TOPERATOR = 3;//二元操作符
	 NAME = 4; //变量名
	 FUNC = 5; //函数名
	 REQUIRE = 6; //导入包
	 NUMBER = 7; //数字
	 BASE = 8; //基础库
	 KEY = 9; //键值
	 STRING = 10; //字符串
	 COMMENT = 30; //注释**/
	public int getTokenColor(int tokenType)
	{
		Colorable element;
		switch (tokenType)
		{
			case Lexer.EXIT:
				element = Colorable.EXIT; //红
				break;
			case Lexer.NIL:
				element = Colorable.NIL; //金
				break;
			case Lexer.NORMAL:
				element = Colorable.FOREGROUND; //黑
				break;
			case Lexer.KEYWORD:
			case Lexer.NAME:
				element = Colorable.NAME; //紫
				break;
			case Lexer.OPERATOR:
				element = Colorable.SECONDARY; //灰
				break;
			case Lexer.TOPERATOR:
				element = Colorable.CARET_BACKGROUND; //亮蓝2
				break;
			case Lexer.FUNC:
			case Lexer.REQUIRE:
				element = Colorable.FUNC; //橄榄绿
				break;
			case Lexer.NUMBER:
				element = Colorable.NUMBER; //亮蓝3
				break;
			case Lexer.BASE:
				element = Colorable.BASE; //亮蓝4
				break;
			case Lexer.KEY:
				element = Colorable.KEYWORD; //黄
				break;
			case Lexer.BOOL:
				element = Colorable.BOOL; //亮红
				break;
			case Lexer.LONGSTRING:
				element = Colorable.LONGSTRING; //红
				break;
			case Lexer.STRING:
				element = Colorable.STRING; //绿
				break;
			case Lexer.COMMENT:
				element = Colorable.COMMENT; //灰
				break;
			default:
				TextWarriorException.fail("Invalid token type");
				element = Colorable.FOREGROUND;
				break;
		}
		return getColor(element);
	}

	/**
	 * Whether this color scheme uses a dark background, like black or dark grey.
	 */
	public abstract boolean isDark();

	private HashMap<Colorable, Integer> generateDefaultColors()
	{
		// High-contrast, black-on-white color scheme
		HashMap<Colorable, Integer> colors = new HashMap<Colorable, Integer>(Colorable.values().length);
		colors.put(Colorable.FOREGROUND, BLACK);
		colors.put(Colorable.BACKGROUND, WHITE);
		colors.put(Colorable.SELECTION_FOREGROUND, WHITE);
		colors.put(Colorable.SELECTION_BACKGROUND, 0xFF97C024);
		colors.put(Colorable.CARET_FOREGROUND, WHITE);
		colors.put(Colorable.CARET_BACKGROUND, LIGHT_BLUE2);
		colors.put(Colorable.CARET_DISABLED, GREY);
		colors.put(Colorable.LINE_HIGHLIGHT, 0x20888888);

		colors.put(Colorable.NON_PRINTING_GLYPH, LIGHT_GREY);
		colors.put(Colorable.COMMENT, COMMENT); //  Eclipse default color
		colors.put(Colorable.KEYWORD, KEYWORD); // Eclipse default color
		colors.put(Colorable.NAME, NAME); // Eclipse default color
		colors.put(Colorable.STRING, MAROON); // Eclipse default color
		colors.put(Colorable.BASE, INDIGO); // Eclipse default color
		colors.put(Colorable.SECONDARY, GREY);
		colors.put(Colorable.NUMBER, NUMBER);
		colors.put(Colorable.FUNC, FUNC);
		colors.put(Colorable.KEY, KEY);
		colors.put(Colorable.LONGSTRING, LONGSTRING);
		colors.put(Colorable.BOOL, BOOL);
		colors.put(Colorable.NIL, NIL);
		colors.put(Colorable.EXIT, EXIT);
		return colors;
	}

	// In ARGB format: 0xAARRGGBB
	private static final int NIL = 0xffe1ad00;
	private static final int EXIT = 0xffff0000;
	/**关键字 黄色**/
	private static final int KEYWORD = 0xff1a5d00;
	/**变量名 紫色**/
	private static final int NAME = 0xffc000ff;
	/**函数名 橄榄绿**/
	private static final int FUNC = 0xFF3F7F5F;
	/**注释 灰**/
	private static final int COMMENT = 0xFF808080;
	/**数字 蓝**/
	private static final int NUMBER = 0xff1e8ae8;
	/**数字 亮蓝**/
	private static final int KEY = 0xff558a00;
	/**字符串 绿**/
	private static final int MAROON = 0xff009b00;
	/**长字符串 红**/
	private static final int LONGSTRING = 0xffef2c74;
	/**boolean 红**/
	private static final int BOOL = 0xd7ff0022;

	/**黑**/
	private static final int BLACK = 0xFF000000;
	/**灰**/
	private static final int GREY = 0xFF808080;
	/**淡灰**/
	private static final int LIGHT_GREY = 0xFFAAAAAA;
	/**靛蓝**/
	private static final int INDIGO = 0xFF2A40FF;
	/**白**/
	private static final int WHITE = 0xFFFFFFE0;
	/**亮淡蓝**/
	private static final int LIGHT_BLUE2 = 0xFF40B0FF;
}
