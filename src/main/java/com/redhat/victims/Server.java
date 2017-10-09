package com.redhat.victims;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Logger;

import com.redhat.victims.domain.Hash;
import com.redhat.victims.fingerprint.JarFile;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle {

	private static final String UPLOADS_DIR = "uploads";
	private static final int DEFAULT_PORT = 8080;
	private static final Logger LOG = Logger.getLogger(Server.class.getName());

	@Override
	public void start(Future<Void> fut) {
		startWebApp((http) -> completeStartup(http, fut));

	}

	private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create().setUploadsDirectory(UPLOADS_DIR));

		router.get("/health").handler(this::health);

		// handle the form
		router.post("/hash").handler(this::hash);

		LOG.info("Starting server at:" + config().getInteger("http.port", DEFAULT_PORT));

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", DEFAULT_PORT),
				next::handle);
	}

	private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
		if (http.succeeded()) {
			fut.complete();
		} else {
			fut.fail(http.cause());
		}
	}

	private void health(RoutingContext routingContext) {
		routingContext.response().setStatusCode(200).end();
	}

	private void hash(RoutingContext ctx) {
		processFiles(ctx);
		ctx.response().end();
	}

	private void processFiles(RoutingContext ctx) {
		Set<FileUpload> fileUploads = ctx.fileUploads();
		JsonArray hashes = new JsonArray();
		for (FileUpload f : fileUploads) {
			LOG.info("Processing file: " + f.fileName());
			Path uploadedFile = Paths.get(f.uploadedFileName());
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(Files.readAllBytes(uploadedFile), f.fileName());
				String filetype = jarFile.getRecord().filetype();
				if (!filetype.equals(".jar")) {
					ctx.response().setStatusCode(501).setStatusMessage("Not Implemented");
					LOG.warning("Invalid file type: " + filetype);
					break;
				}
				Hash hash = new Hash(jarFile);
				hashes.add(hash.asJson());
			} catch (IOException e) {
				ctx.response().setStatusCode(500).setStatusMessage(e.getMessage());
				LOG.severe("Error reading from file: " + f.uploadedFileName());
			} finally {
				cleanup();
			}
		}
	}

	private void cleanup() {
		try {
			LOG.fine("Cleaning up files in " + UPLOADS_DIR);
			File uploadsDir = new File(UPLOADS_DIR);
			for (File file : uploadsDir.listFiles())
				if (!file.isDirectory())
					file.delete();
		} catch (Exception e) {
			LOG.severe("Error deleting files in " + UPLOADS_DIR);
		}
	}

}
