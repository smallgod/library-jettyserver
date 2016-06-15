/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.library.jettyhttpserver;

import com.library.jettyhttpserver.utilities.Logging;
import java.io.FileNotFoundException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.webapp.Configuration;
import org.slf4j.Logger;

/**
 *
 * @author smallgod
 */
public class CustomJettyServer {

    private HttpConfiguration defaultHTTPConfig;
    private final String webDescriptor;
    private final String resourceBase;
    private final String contextPath;
    private final String webAppWarFile;
    private int defaultHttpPort;
    private final Logging logging;
    private Server jettyServer;
    //PUT IN CONFIGS FILE
    //public static final String JETTY_HOME = System.getProperty("jetty.home", "..");

    private final ServerConnectorFactory serverConnectorFactory;
    private final ServerHandlerFactory serverHandlerFactory;

//    public CustomJettyServer() {
//    }
//
//    public CustomJettyServer(int serverPort) {
//        super(serverPort);
//    }
//
//    public CustomJettyServer(InetSocketAddress isa) {
//        super(isa);
//    }
//
//    public CustomJettyServer(ThreadPool tp) {
//        super(tp);
//    }
    public CustomJettyServer(String webDescriptor, String resourceBase, String contextPath, String webAppWarFile, int defaultHttpPort, Logger logger) {

        logging = new Logging(logger);

        jettyServer = new Server();

        this.webDescriptor = webDescriptor;
        this.resourceBase = resourceBase;
        this.contextPath = contextPath;
        this.webAppWarFile = webAppWarFile;
        this.defaultHttpPort = defaultHttpPort;

        defaultHTTPConfig = new HttpConfiguration();
        serverConnectorFactory = new ServerConnectorFactory(logging);
        serverHandlerFactory = new ServerHandlerFactory(logging);

    }

    public void defaultConfigure() {

        // Resource jettyConfig = Resource.newSystemResource("jetty.xml");
        // XmlConfiguration configuration = new XmlConfiguration(jettyConfig.getInputStream());
        // Server localServer = (Server)configuration.configure();
        // Enable parsing of jndi-related parts of web.xml and jetty-env.xml
        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(jettyServer);
        logging.debug("Class list size   :: " + classlist.size());
        // classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
        logging.debug("Class list members:: " + classlist.get(0) + " " + classlist.get(1) + " " + classlist.get(2) + " " + classlist.get(3) + " " + classlist.get(4));

        //add only default configs that will enable calling this.start();
        this.addDefaultConnector(defaultHttpPort);
        this.addDefaultHandler();
        //Connector defaultConnector = serverConnectorFactory.createHttpConnector(jettyServer, defaultHTTPConfig, defaultHttpPort);
        //Handler addWebAppContextHandler = serverHandlerFactory.createWebAppContextHandler(contextPath, resourceBase, webDescriptor);

        //jettyServer.addConnector(defaultConnector);
        //jettyServer.setHandler(addWebAppContextHandler);
        //set HTTPConfigs for desired connectors
        //set desired connectors
        //set handlers
    }

    public void addHTTPConfigs(
            int OUTPUT_BUFFER_SIZE,
            int REQUEST_HEADER_SIZE,
            int RESPONSE_HEADER_SIZE,
            boolean isSendServerVersion,
            boolean isSendDateHeader
    ) {

        defaultHTTPConfig = serverConnectorFactory.createHTTPConfigs(jettyServer, defaultHTTPConfig, OUTPUT_BUFFER_SIZE, REQUEST_HEADER_SIZE, RESPONSE_HEADER_SIZE, isSendServerVersion, isSendDateHeader);

    }

    public ServerConnector addConnectorConfigs(
            ServerConnector serverConnector,
            long IDLE_TIMEOUT
    ) {
        return serverConnectorFactory.setConnectorConfigs(serverConnector, IDLE_TIMEOUT);
    }

    private void addDefaultConnector(final int HTTP_PORT) {

        addHttpConnector(HTTP_PORT);
    }

    public void addHttpConnector(final int HTTP_PORT) {

        Connector httpConnector = serverConnectorFactory.createHttpConnector(jettyServer, defaultHTTPConfig, HTTP_PORT);
        jettyServer.addConnector(httpConnector);
    }

    public void addHttpsConnector(
            final int HTTPS_PORT,
            final String KEYSTORE_PATH,
            final String KEYSTORE_PASS,
            final String KEYSTORE_MGR_PASS
    ) throws FileNotFoundException {

        Connector httpsConnector = serverConnectorFactory.createHttpsConnector(jettyServer, defaultHTTPConfig, HTTPS_PORT, KEYSTORE_PATH, KEYSTORE_PASS, KEYSTORE_MGR_PASS);
        jettyServer.addConnector(httpsConnector);

    }

    public void addAdminConnector(final int ADMIN_PORT) {

        Connector adminConnector = serverConnectorFactory.createAdminConnector(jettyServer, defaultHTTPConfig, ADMIN_PORT);
        jettyServer.addConnector(adminConnector);
    }

    /**
     * Add web app context handler with the default values passed in constructor
     *
     */
    public void addWebAppContextHandler() {
        Handler webAppContextHandler = serverHandlerFactory.createWebAppContextHandler(contextPath, resourceBase, webDescriptor);
        jettyServer.setHandler(webAppContextHandler);
    }

    /**
     * Add web app context handler with the default values passed in constructor
     *
     * @return
     */
    private void addDefaultHandler() {
        addWebAppContextHandler();
    }

    /**
     * Looks for a matching file in the local directory to serve
     *
     * @param welcomeFiles
     * @param isDirectoriesListed
     */
    public void addResourceHandler(String[] welcomeFiles, Boolean isDirectoriesListed) {
        Handler resourceHandler = serverHandlerFactory.createResourceHandler(welcomeFiles, resourceBase, isDirectoriesListed);
        jettyServer.setHandler(resourceHandler);
    }

    /**
     * A ContextHandler is a HandlerWrapper that responds only to requests that
     * have a URI prefix that matches the configured context path. Requests that
     * match the context path have their path methods updated accordingly, and
     * the following optional context features applied as appropriate: A Thread
     * Context classloader. A set of attributes A set of init parameters A
     * resource base (aka document root) A set of virtual host names. Requests
     * that don't match are not handled.
     *
     * @param welcomeFiles
     */
    public void addContextHandler(String[] welcomeFiles) {
        Handler contextHandler = serverHandlerFactory.createContextHandler(welcomeFiles, resourceBase, contextPath);
        jettyServer.setHandler(contextHandler);
    }

    /**
     * A ServletContextHandler is a specialization of ContextHandler with
     * support for standard servlets.
     *
     * @param welcomeFiles
     */
    public void getServletContextHandler(String[] welcomeFiles) {
        Handler servletHander = serverHandlerFactory.createServletContextHandler(welcomeFiles, resourceBase, contextPath);
        jettyServer.setHandler(servletHander);
    }

    /**
     * Add one or multiple connectors
     *
     * @param connectors
     */
    private void addConnectors(Connector... connectors) {
        jettyServer.setConnectors(connectors);
        //jettyServer.setConnectors(new Connector[]{ connector0, connector1, ssl_connector });
    }

    /**
     * Exactly one connector can be set here
     *
     * @param connector
     */
    private void addConnector(Connector connector) {
        jettyServer.addConnector(connector);
    }

    /**
     * Exactly one handler can be set here
     *
     * @param handler
     */
    private void addHandler(Handler handler) {
        jettyServer.setHandler(handler);
    }

    /**
     * Add one or multiple handlers
     *
     * @param handlers
     */
    private void addHandlers(Handler... handlers) {
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        //contexts.setHandlers(new Handler[] { context0, webapp });
        contexts.setHandlers(handlers);
        jettyServer.setHandler(contexts);

    }

    /**
     * Start Jetty server
     *
     * @return true if server is started
     * @throws java.lang.Exception
     */
    public boolean startServer() throws Exception {

        boolean isStarted = Boolean.FALSE;

        // Extra options
        jettyServer.setDumpAfterStart(true);
        jettyServer.setDumpBeforeStop(true);
        jettyServer.setStopAtShutdown(true);

        if (!(jettyServer.isRunning() || jettyServer.isStarting() || jettyServer.isStarted())) {

            jettyServer.start();
            jettyServer.dumpStdErr();
            //jettyServer.join(); //think of putting this inside an executor so that it doesn't hang

            if (jettyServer.isStarted()) {

                logging.info("Yoyoyoyoo!!! Jetty server is started na bidi");
                isStarted = Boolean.TRUE;

            } else {
                logging.info("Couldn't start server despite a non-conflicting state: " + jettyServer.getState());
            }

        } else {
            logging.warn("Can't start server - state: " + jettyServer.getState());
        }

        return isStarted;

    }

    /**
     * Try to stop the Jetty server
     *
     * @return true if server is stopped
     * @throws java.lang.Exception
     */
    public boolean stopServer() throws Exception {

        boolean isStopped = Boolean.FALSE;

        jettyServer.stop();

        if (jettyServer.isStopped() || jettyServer.isStopping()) {
            logging.info("Jetty server successful stopped or is stopping.. - " + jettyServer.getState());
            isStopped = Boolean.TRUE;
        }

        return isStopped;
    }
}
