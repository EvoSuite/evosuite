/*
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * JsMin
 * Javascript Compressor
 * http://www.crockford.com/
 * http://www.smallsharptools.com/
*/

dp.sh.Brushes.Java=function()
{var keywords='abstract assert boolean break byte case catch char class const '+'continue default do double else enum extends '+'false final finally float for goto if implements import '+'instanceof int interface long native new null '+'package private protected public return '+'short static strictfp super switch synchronized this throw throws true '+'transient try void volatile while';this.regexList=[{regex:dp.sh.RegexLib.SingleLineCComments,css:'comment'},{regex:dp.sh.RegexLib.MultiLineCComments,css:'comment'},{regex:dp.sh.RegexLib.DoubleQuotedString,css:'string'},{regex:dp.sh.RegexLib.SingleQuotedString,css:'string'},{regex:new RegExp('\\b([\\d]+(\\.[\\d]+)?|0x[a-f0-9]+)\\b','gi'),css:'number'},{regex:new RegExp('(?!\\@interface\\b)\\@[\\$\\w]+\\b','g'),css:'annotation'},{regex:new RegExp('\\@interface\\b','g'),css:'keyword'},{regex:new RegExp(this.GetKeywords(keywords),'gm'),css:'keyword'}];this.CssClass='dp-j';this.Style='.dp-j .annotation { color: #646464; }'+'.dp-j .number { color: #C00000; }';}
dp.sh.Brushes.Java.prototype=new dp.sh.Highlighter();dp.sh.Brushes.Java.Aliases=['java'];
