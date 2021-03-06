package org.jacorb.test.common.launch;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.taskdefs.optional.junit.BatchTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import org.jacorb.test.common.TestUtils;
import org.jacorb.util.ObjectUtil;

/**
 * This is the main class for launching regression tests.  It takes care
 * of launching the client side (on which the JUnit tests are actually executed)
 * against an appropriate JacORB version.  The client-side code itself
 * launches servers when appropriate.  This is the invocation syntax:
 *
 *   java [ -Djacorb.test.client.version=CLIENT_VERSION ]
 *        [ -Djacorb.test.server.version=SERVER_VERSION ]
 *        [ -Djacorb.test.coverage=on/off ]
 *        org.jacorb.test.common.launch.TestLauncher
 *        TESTSUITE
 *
 * Here, CLIENT_VERSION and SERVER_VERSION are ids of available JacORB
 * installations, as specified in the file
 * $JACORB_HOME/test/regression/test.properties.
 * The third optional property, jacorb.test.coverage, specifies whether
 * coverage information should be collected during this test run or not.
 * The final argument, after the name of the class itself, is the name
 * of the TESTSUITE to execute (e.g. org.jacorb.test.AllTest).
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: TestLauncher.java,v 1.14 2006-11-30 13:12:58 alphonse.bendt Exp $
 */
public class TestLauncher extends Task
{
    private static final DateFormat dateStringFormatter =
        new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss Z");

    private final List formatters = new ArrayList();
    private final List classpath = new ArrayList();

    private final Date testDate = new java.util.Date();
    private final Properties props = new Properties();

    private String serverVersion = "cvs";
    private String clientVersion = "cvs";
    private long timeout = 15000;
    private String maxMemory = "64m";

    private String suite;
    private boolean coverage;
    private String errorProperty;
    private boolean useSSL;
    private boolean useIMR;
    private long testTimeout = 60 * 60 * 1000;

    private File testDir;

    private File outDir;

    private boolean showOutput;

    private final List batchTests = new ArrayList();

    private void printTestHeader (PrintWriter out)
    {
        out.println("-------------------------------------------------------------------------------");
        out.println();
        out.println("  JacORB Regression Test Report");
        out.println();
        out.println("  Date:     " + getTestDateString());
        out.println("  User:     " + getTestUser());
        out.println("  Platform: " + getTestPlatform());
        out.println();
        out.println("  Client Version:   " + getClientVersion());
        out.println("  Server Version:   " + getServerVersion());
        out.println("  Coverage:         " + (getCoverage() ? "yes" : "no"));
        out.println("  SSL:              " + (useSSL ? "yes" : "no"));
        out.println("  IMR:              " + (useIMR ? "yes" : "no"));
        out.println("  Timeout:          " + timeout);
        out.println("  -Xmx:             " + maxMemory);
        out.println();
        out.println("-------------------------------------------------------------------------------");
        out.println();
    }

    private void printTestHeader (PrintStream out)
    {
        PrintWriter writer = new PrintWriter (out);
        printTestHeader(writer);
        writer.flush();
    }

    private String getTestDateString()
    {
        return dateStringFormatter.format (testDate);
    }

    public void setCoverage(boolean value)
    {
        coverage = value;
    }

    private boolean getCoverage()
    {
        return coverage;
    }

    public void setClientVersion(String value)
    {
        clientVersion = value;
    }

    private String getClientVersion()
    {
        return clientVersion;
    }

    public void setServerVersion(String value)
    {
        serverVersion = value;
    }

    private String getServerVersion()
    {
        return serverVersion;
    }

    private String getTestUser()
    {
        return System.getProperty ("user.name", "<unknown>");
    }

    private String getTestPlatform()
    {
        return "java " + System.getProperty ("java.version")
             + " (" + System.getProperty ("java.vendor") + ") "
             + System.getProperty ("os.name") + " "
             + System.getProperty ("os.version") + " ("
             + System.getProperty ("os.arch") + ")";
    }

    public void setTestTimeout(long timeout)
    {
        testTimeout = timeout;
    }

    public void setOutdir(File value)
    {
        outDir = value;
    }

    public void setSuite(String suite)
    {
        this.suite = suite;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public void setMaxMemory(String value)
    {
        this.maxMemory = value;

        props.setProperty("jacorb.test.maxmemory", value);
    }

    public void setErrorProperty(String value)
    {
        errorProperty = value;
    }

    public void setIMR(boolean useIMR)
    {
        this.useIMR = useIMR;
    }

    public void setSSL(boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    public void setVerbose(boolean value)
    {
        System.setProperty("jacorb.test.verbose", Boolean.toString(value));
        props.setProperty("jacorb.test.verbose", Boolean.toString(value));
    }

    public void addConfiguredJVMArg(Parameter var)
    {
        props.setProperty(var.getName(), var.getValue());
    }

    public void addConfiguredClasspath(Path path)
    {
        classpath.add(path);
    }

    public void addFormatter(FormatterElement formatter)
    {
        formatters.add(formatter);
    }

    public void setTestDir(File dir)
    {
        testDir = dir;
    }

    public void setShowoutput(boolean value)
    {
        showOutput = value;
    }

    public BatchTest createBatchTest()
    {
        BatchTest test = new BatchTest(getProject());
        batchTests.add(test);
        return test;
    }

    public void execute() throws BuildException
    {
        if (outDir == null)
        {
            throw new BuildException("need to specify the attribute outdir");
        }

        if (testDir == null)
        {
            throw new BuildException("need to specify the attribute testdir");
        }

        if (suite == null && batchTests.isEmpty())
        {
            throw new BuildException("need to either specify the attribute suite or add a nested batch element");
        }

        if (suite != null && !batchTests.isEmpty())
        {
            throw new BuildException("need to either specify the attribute suite OR add a nested batch element. not both!");
        }

        try
        {
            executeInternal();
        }
        catch (IOException e)
        {
            throw new BuildException(e);
        }
        catch (InterruptedException e)
        {
            throw new BuildException(e);
        }
    }

    private void executeInternal() throws IOException, InterruptedException
    {
        outDir.mkdir();

        final String testHome = TestUtils.osDependentPath(testDir.getAbsolutePath());

        // TODO don't do that
        // testhome should be passed in to JacORBLauncher instead
        System.setProperty("jacorb.test.home", testHome);

        final Properties props = newPropertiesForTest(testHome);

        final List allTests;

        if (suite != null)
        {
            allTests = Collections.singletonList(suite);
        }
        else
        {
            allTests = new ArrayList();
            Iterator i = batchTests.iterator();
            while(i.hasNext())
            {
                BatchTest test = (BatchTest) i.next();
                Enumeration e = test.elements();
                while(e.hasMoreElements())
                {
                    JUnitTest jtest = (JUnitTest) e.nextElement();
                    allTests.add(jtest.getName());
                }
            }
        }

        iterateTests(allTests, props);
    }

    private void iterateTests(List tests, final Properties props) throws IOException, InterruptedException
    {
        printHeader();

        final Iterator i = tests.iterator();
        while(i.hasNext())
        {
            String suite = (String) i.next();

            final String[] processArgs = newArgsForTest(suite);

            log("TestLauncher ARGS:\n" + format(processArgs), Project.MSG_VERBOSE);
            log("TestLauncher PROPS:\n" + format(props), Project.MSG_VERBOSE);
            log("TestLauncher CLASSPATH:\n    " + formatClasspath(), Project.MSG_VERBOSE);

            final Process process = startJUnitProcess(props, processArgs);

            final ExecuteWatchdog watchDog;

            if (testTimeout == 0)
            {
                watchDog = null;
            }
            else
            {
                watchDog = new ExecuteWatchdog(testTimeout);
                watchDog.start(process);
            }

            int retCode = process.waitFor();

            if (watchDog != null && watchDog.killedProcess())
            {
                JUnitTest test = new JUnitTest(suite);

                try
                {
                    logTimeout((FormatterElement[]) formatters.toArray(new FormatterElement[formatters.size()]), test);
                }
                catch (Exception e)
                {
                    throw new BuildException();
                }

                getProject().setNewProperty(errorProperty, "true");
            }

            if (errorProperty != null && retCode != 0)
            {
                getProject().setNewProperty(errorProperty, "true");
            }
        }
    }

    private Process startJUnitProcess(final Properties props, final String[] processArgs)
    {
        final String mainClass = "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner";
        final Launcher launcher = JacORBLauncher.getLauncher
        (
            getClientVersion(), getCoverage(), buildClasspath(), props, mainClass, processArgs
        );

        final Process process = launcher.launch();

        final LogStreamHandler logHandler = new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN);
        logHandler.setProcessErrorStream(process.getErrorStream());
        logHandler.setProcessOutputStream(process.getInputStream());
        logHandler.setProcessInputStream(process.getOutputStream());
        logHandler.start();
        return process;
    }

    private void printHeader() throws IOException
    {
        final PrintWriter out = new PrintWriter(new FileWriter(new File(outDir, "header.txt")));
        try
        {
            printTestHeader(out);
        }
        finally
        {
            out.close();
        }

        printTestHeader (System.out);
    }

    private String format(String[] args)
    {
        return format(Arrays.asList(args));
    }

    private String[] newArgsForTest(String suite)
    {
        List args = new ArrayList();
        args.add(suite);
        args.add("printsummary=withOutAndErr");
        args.add("showoutput=" + Boolean.toString(showOutput));

        Iterator i = formatters.iterator();
        while(i.hasNext())
        {
            FormatterElement formatter = (FormatterElement) i.next();
            if (formatter.shouldUse(this))
            {
                StringBuffer buffer = new StringBuffer("formatter=");
                buffer.append(formatter.getClassname());
                final File outputFile = getOutput(suite, formatter);
                if (outputFile != null)
                {
                    buffer.append(',');
                    buffer.append(outputFile);
                }

                args.add(buffer.toString());
            }
        }

        String[] processArgs = (String[]) args.toArray(new String[args.size()]);

        // the bootclasspath is determined by the launcher.

        return processArgs;
    }

    private Properties newPropertiesForTest(String testHome) throws IOException
    {
        Properties props = new Properties();

        props.setProperty("jacorb.orb.singleton.log.verbosity", "1");
        props.setProperty("jacorb.config.log.verbosity", "0");

        props.putAll(this.props);

        props.put("jacorb.test.coverage", Boolean.toString(coverage));
        props.put("jacorb.test.ssl", Boolean.toString(useSSL));
        props.put("jacorb.test.imr", Boolean.toString(useIMR));
        props.put("jacorb.test.client.version", getClientVersion());
        props.put("jacorb.test.server.version", getServerVersion());
        props.put("jacorb.test.timeout", Long.toString(timeout));
        props.put("jacorb.test.outdir", outDir.getAbsolutePath());
        props.put("jacorb.test.home", testHome);

        try
        {
            ObjectUtil.classForName("org.jacorb.test.orb.rmi.FixSunDelegateBug");
            props.put("javax.rmi.CORBA.UtilClass",
                      "org.jacorb.test.orb.rmi.FixSunDelegateBug");
            log("Using org.jacorb.test.orb.rmi.FixSunDelegateBug", Project.MSG_INFO);
        }
        catch(NoClassDefFoundError e)
        {
        }
        catch(Exception e)
        {
        }

        if (TestUtils.isWindows())
        {
            try
            {
                String systemRoot = TestUtils.systemRoot();
                props.put("jacorb.SystemRoot", systemRoot);
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (RuntimeException e)
            {
                throw e;
            }
        }

        if (getCoverage())
        {
            String coveragePath = outDir + "/coverage-client.ec";
            coveragePath = TestUtils.osDependentPath(coveragePath);
            props.put ("emma.coverage.out.file", coveragePath);
            props.put ("emma.verbosity.level", "quiet" );
        }

        return props;
    }

    protected File getOutput(String suite, FormatterElement fe)
    {
        boolean useFile = true;
        try
        {
            Method method = fe.getClass().getDeclaredMethod("getUseFile", null);
            method.setAccessible(true);
            useFile = ((Boolean)method.invoke(fe, null)).booleanValue();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }

        if (!useFile)
        {
            return null;
        }

        String filename = "TEST-" + suite + fe.getExtension();
        File destFile = new File(outDir, filename);
        String absFilename = destFile.getAbsolutePath();
        return getProject().resolveFile(absFilename);
    }

    private String formatClasspath()
    {
        StringBuffer buffer = new StringBuffer();

        buildClasspath(buffer, classpath, "\n    ");

        return buffer.toString();
    }

    private String buildClasspath()
    {
        StringBuffer buffer = new StringBuffer();

        buildClasspath(buffer, classpath, File.pathSeparator);

        return buffer.toString();
    }

    private void buildClasspath(StringBuffer buffer, final List list, String separator)
    {
        Iterator i = list.iterator();

        while(i.hasNext())
        {
            Path path = (Path) i.next();

            String[] entries = path.list();

            for (int j = 0; j < entries.length; j++)
            {
                if (j > 0)
                {
                    buffer.append(separator);
                }

                buffer.append(entries[j]);
            }
        }
    }

    private static String format(List list)
    {
        StringBuffer buffer = new StringBuffer();
        int x = 1;
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            String string = (String) i.next();
            buffer.append("    ");
            buffer.append(x++);
            buffer.append(": ");
            buffer.append(string);
            buffer.append('\n');
        }

        return buffer.toString();
    }

    private static String format(Properties props)
    {
        StringBuffer buffer = new StringBuffer();

        for (Iterator i = props.keySet().iterator(); i.hasNext();)
        {
            String key = (String) i.next();
            buffer.append("    ");
            buffer.append(key);
            buffer.append('=');
            buffer.append(props.getProperty(key));
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * copied from ant's JUnitTask
     */
    private void logTimeout(FormatterElement[] feArray, JUnitTest test) throws Exception
    {
        test.setCounts(1, 0, 1);
        test.setProperties(getProject().getProperties());
        for (int i = 0; i < feArray.length; i++)
        {
            FormatterElement fe = feArray[i];

            if (!fe.shouldUse(this))
            {
                continue;
            }

            File outFile = getOutput(test.getName(), fe);

            Method method = fe.getClass().getDeclaredMethod("createFormatter", new Class[] {ClassLoader.class});
            method.setAccessible(true);

            JUnitResultFormatter formatter = (JUnitResultFormatter) method.invoke(fe, new Object[] {getClass().getClassLoader()});
            if (outFile != null && formatter != null )
            {
                try
                {
                    OutputStream out = new FileOutputStream(outFile);
                    addTimeout(test, formatter, out);
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * copied from ant's JUnitTask
     */
    private void addTimeout(JUnitTest test,
                            JUnitResultFormatter formatter,
                            OutputStream out) {


        formatter.setOutput(out);
        formatter.startTestSuite(test);

        //the trick to integrating test output to the formatter, is to
        //create a special test class that asserts a timout occurred,
        //and tell the formatter that it raised.
        Test t = new Test() {
                public int countTestCases() { return 1; }
                public void run(TestResult r) {
                    throw new AssertionFailedError("Timeout occurred");
                }
            };
        formatter.startTest(t);
        formatter.addError(t, new AssertionFailedError("Timeout occurred"));
        formatter.endTestSuite(test);
    }
}
