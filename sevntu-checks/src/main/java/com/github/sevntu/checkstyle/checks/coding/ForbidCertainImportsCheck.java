////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2011  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.github.sevntu.checkstyle.checks.coding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;
import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Forbids certain imports usage in certain packages or/and classes. <br/>
 * <br/>
 * You can configure this check using the following parameters:
 * <ol>
 * <li>Package qualified name regexp;</li>
 * <li>Class qualified name regexp;</li>
 * <li>Forbidden imports regexp;</li>
 * <li>Forbidden imports excludes regexp.</li>
 * </ol>
 * Variants of configuration: <br/>
 * <dl>
 * <li>Only packages are defined. The verification will spread on all classes
 * contained in these packages.</li>
 * <li>Only classes are defined. The NEW operation will be checked in defined
 * classes. The IMPORT operation will be checked in all file that contain
 * defined classes.</li>
 * <li>Classes and packages are defined. The verification will spread only on
 * defined classes contained in defined packages</li>
 * </dl>
 * <br/>
 * This check loads packages qualified names without words "package","import",
 * "class" and semicolons, so, please, do NOT include "package", "class" or
 * "import" words (or semicolons) into config regexps.<br/>
 * <br/>
 * Real-life example of usage: forbid to use all "*.ui.*" packages in "*.dao.*"
 * packages, but ignore all Exception imports (such as
 * <b>org.springframework.dao.InvalidDataAccessResourceUsageException</b>). For
 * doing that, you should to use the following check parameters: <br/>
 * <br/>
 * <dl>
 * <li>Package name regexp = ".*ui.*"</li>
 * <li>Class name regexp = ""</li>
 * <li>Forbidden imports regexp = ".*dao.*"</li>
 * <li>Forbidden imports excludes regexp = "^.+Exception$"</li>
 * </dl>
 * <br/>
 * Another example: forbid to use all "*.ui.*" packages in ".*First.*" classes.
 * For doing that, you should to use the following check parameters: <br/>
 * <br/>
 * <dl>
 * <li>Package name regexp = ""</li>
 * <li>Class name regexp = ".*First.*"</li>
 * <li>Forbidden imports regexp = "*.ui.*"</li>
 * <li>Forbidden imports excludes regexp = ""</li>
 * </dl>
 * <br/>
 * You can cover more sophisticated rules by means of few check instances.<br/>
 * <br/>
 * @author <a href="mailto:Daniil.Yaroslavtsev@gmail.com"> Daniil
 *         Yaroslavtsev</a>
 * @author <a href="mailto:vadim.panasiuk@gmail.com"> Vadim Panasiuk</a>
 */
public class ForbidCertainImportsCheck extends Check
{

    /**
     * The key is pointing to the warning message text in "messages.properties"
     * file.
     */
    protected static final String MSG_KEY = "forbid.certain.imports";

    /**
     * in order to Pattern.compile(matchWithAllStringRegExp).matcher("any string")
     * always return true
     */
    private String matchWithAllStringRegExp = ".*";
    /**
     * Pattern for matching package fully qualified name (sets the scope of
     * affected packages).
     */
    private Pattern mPackageNamesRegexp;

    /**
     * Pattern for matching class fully qualified name (sets the scope of
     * affected classes).
     */
    private Pattern mClassNamesRegexp;

    /**
     * Pattern for matching forbidden imports.
     */
    private Pattern mForbiddenImportsRegexp;

    /**
     * Pattern for excluding imports from checking.
     */
    private Pattern mForbiddenImportsExcludesRegexp;

    /**
     * True, if currently processed package fully qualified name matches regexp
     * is provided by user.
     */
    private boolean mPackageMatches;

    /**
     * True, if currently processed class fully qualified name matches regexp is
     * provided by user.
     */
    private boolean mClassMatches;

    /**
     * Map containing DetailAST node and text related to forbidden imports.
     */
    private HashMap<DetailAST, String> mForbiddenImports = Maps.newHashMap();

    /**
     * Sets the regexp for matching package fully qualified name.
     * @param aPackageNameRegexp
     *        regexp for package fully qualified name matching.
     */
    public void setPackageNameRegexp(String aPackageNameRegexp)
    {
        if (aPackageNameRegexp != null) {
            if (aPackageNameRegexp.length() == 0)
            {
                mPackageNamesRegexp = Pattern.compile(matchWithAllStringRegExp);
            }
            else
            {
                mPackageNamesRegexp = Pattern.compile(aPackageNameRegexp);
            }
        }
    }

    /**
     * Sets the regexp for matching class fully qualified name.
     * @param aClassNameRegexp
     *        regexp for class fully qualified name matching.
     */
    public void setClassNameRegexp(String aClassNameRegexp)
    {
        if (aClassNameRegexp.length() == 0)
        {
            mClassNamesRegexp = Pattern.compile(matchWithAllStringRegExp);
        }
        else
        {
            mClassNamesRegexp = Pattern.compile(aClassNameRegexp);
        }
    }

    /**
     * Gets the regexp is used for matching forbidden imports.
     * @return regexp for forbidden imports matching.
     */
    public String getForbiddenImportRegexp()
    {
        return mForbiddenImportsRegexp.toString();
    }

    /**
     * Sets the regexp for matching forbidden imports.
     * @param aForbiddenImportsRegexp
     *        regexp for matching forbidden imports.
     */
    public void setForbiddenImportsRegexp(String aForbiddenImportsRegexp)
    {
        if (aForbiddenImportsRegexp != null) {
            mForbiddenImportsRegexp = Pattern.compile(aForbiddenImportsRegexp);
        }
    }

    /**
     * Gets the regexp for excluding imports from checking.
     * @return regexp for excluding imports from checking.
     */
    public String getForbiddenImportsExcludesRegexp()
    {
        return mForbiddenImportsExcludesRegexp.toString();
    }

    /**
     * Sets the regexp for excluding imports from checking.
     * @param aForbiddenImportsExcludesRegexp
     *        String contains a regexp for excluding imports from checking.
     */
    public void setForbiddenImportsExcludesRegexp(String
            aForbiddenImportsExcludesRegexp)
    {
        if (aForbiddenImportsExcludesRegexp != null) {
            mForbiddenImportsExcludesRegexp = Pattern
                    .compile(aForbiddenImportsExcludesRegexp);
        }
    }

    @Override
    public int[] getDefaultTokens()
    {
        final int[] defaultTokens;
        if (mPackageNamesRegexp == null || mClassNamesRegexp == null
                || mForbiddenImportsRegexp == null
                || mForbiddenImportsExcludesRegexp == null) {
            defaultTokens = new int[] {};
        }
        else {
            defaultTokens = new int[] { TokenTypes.PACKAGE_DEF,
                    TokenTypes.IMPORT, TokenTypes.LITERAL_CLASS,
                    TokenTypes.LITERAL_NEW };
        }
        return defaultTokens;
    }

    @Override
    public void visitToken(DetailAST aAst)
    {
        switch (aAst.getType()) {
        case TokenTypes.PACKAGE_DEF:
            if (mPackageNamesRegexp != null) {
                final String packageQualifiedName = getText(aAst);
                mPackageMatches = mPackageNamesRegexp.matcher(
                        packageQualifiedName)
                        .matches();
            }
            break;
        case TokenTypes.LITERAL_CLASS:
            if (mClassNamesRegexp != null) {
                final String classQualifiedName = getClassIdent(aAst);
                mClassMatches = mClassNamesRegexp.matcher(classQualifiedName)
                        .matches();
                if (mClassMatches && mPackageMatches
                        && !mForbiddenImports.isEmpty())
                {
                    log(mForbiddenImports);
                }
            }
            break;
        case TokenTypes.IMPORT:
            if (mForbiddenImportsRegexp != null
                    && mForbiddenImportsExcludesRegexp != null)
            {
                final String importQualifiedText = getText(aAst);
                if (isImportForbidden(importQualifiedText))
                {
                    if (mPackageMatches && mClassMatches)
                    {
                        log(aAst, importQualifiedText);
                    }
                    else
                    {
                        mForbiddenImports.put(aAst, importQualifiedText);
                    }

                }
            }
            break;
        case TokenTypes.LITERAL_NEW:
            if (mForbiddenImportsRegexp != null
                    && mForbiddenImportsExcludesRegexp != null
                    && (mPackageMatches && mClassMatches)
                    && aAst.findFirstToken(TokenTypes.DOT) != null)
            {
                final String importQualifiedText = getText(aAst);
                if (isImportForbidden(importQualifiedText)) {
                    log(aAst, importQualifiedText);
                }
            }
            break;
        default:
            final String className = this.getClass().getSimpleName();
            final String tokenType = TokenTypes.getTokenName(aAst.getType());
            final String tokenDescription = aAst.toString();
            final String message = String.format(
                    "%s got the wrong input token: %s (%s)",
                    className, tokenType, tokenDescription);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks if given import both matches 'include' and not matches 'exclude'
     * patterns.
     * @param aImportText
     *        package fully qualified name
     * @return true is given import is forbidden in current classes package,
     *         false otherwise
     */
    private boolean isImportForbidden(String aImportText)
    {
        return mForbiddenImportsRegexp.matcher(aImportText).matches()
                && !mForbiddenImportsExcludesRegexp.matcher(aImportText)
                        .matches();
    }

    /**
     * Logs message on the part of code.
     * @param aNodeToWarn
     *        A DetailAST node is pointing to the part of code to warn on.
     * @param aImportText
     *        import to be warned.
     */
    private void log(DetailAST aNodeToWarn, String aImportText)
    {
        log(aNodeToWarn.getLineNo(), MSG_KEY,
                getForbiddenImportRegexp(), aImportText);
    }

    /**
     * Logs message on the part of code.
     * @param forbiddenImports
     *        A HashMap<DetailAST, String> contained node and text for warning.
     */
    private void log(HashMap<DetailAST, String> forbiddenImports)
    {
        for (Iterator<Map.Entry<DetailAST, String>> iterator = forbiddenImports
                .entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry<DetailAST, String> pairs =
                    (Map.Entry<DetailAST, String>) iterator.next();
            DetailAST aNodeToWarn = (DetailAST) pairs.getKey();
            String aImportText = (String) pairs.getValue();
            log(aNodeToWarn.getLineNo(), MSG_KEY,
                    getForbiddenImportRegexp(), aImportText);
            iterator.remove();
        }

    }

    /**
     * Gets package/import text representation from node of PACKAGE_DEF or
     * IMPORT type.
     * @param aPackageDefOrImportNode
     *        - DetailAST node is pointing to package or import definition
     *        (should be a PACKAGE_DEF or IMPORT type).
     * @return The fully qualified name of package or import without
     *         "package"/"import" words or semicolons.
     */
    private static String getText(DetailAST aPackageDefOrImportNode)
    {
        String result = null;

        final DetailAST identNode = aPackageDefOrImportNode
                .findFirstToken(TokenTypes.IDENT);

        if (identNode == null) {
            final DetailAST parentDotAST = aPackageDefOrImportNode
                    .findFirstToken(TokenTypes.DOT);
            if (parentDotAST != null) {
                final FullIdent dottedPathIdent = FullIdent
                        .createFullIdentBelow(parentDotAST);
                final DetailAST nameAST = parentDotAST.getLastChild();
                result = dottedPathIdent.getText() + "." + nameAST.getText();
            }
        }
        else {
            result = identNode.getText();
        }
        return result;
    }

    /**
     * Gets class text representation from node of LITERAL_CLASS type.
     * @param aClassDefNode
     *        - DetailAST node is pointing to class definition (should be a
     *        LITERAL_CLASS type).
     * @return The fully qualified name of class without "class" words or
     *         semicolons.
     */
    private static String getClassIdent(DetailAST aClassDefNode)
    {
        String result = "";

        DetailAST nextNodeToClass = aClassDefNode.getNextSibling();
        if (nextNodeToClass.getType() == TokenTypes.IDENT)
        {
            result = nextNodeToClass.getText();
        }
        return result;
    }

}
