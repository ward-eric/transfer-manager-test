package com.test;

import org.slf4j.Logger;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.utils.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.slf4j.LoggerFactory.getLogger;

public class TransferManagerTest {

  private static final Logger LOG = getLogger(TransferManagerTest.class);

  public static void main(String[] args) {
    if (args.length != 1) {
      LOG.info("Must supply a bucket name!");
    }
    String bucketName = args[0];
    int threadCount = 100;

    ExecutorService executorService = Executors.newFixedThreadPool(
      threadCount,
      new ThreadFactoryBuilder().daemonThreads(true).threadNamePrefix("test-worker").build()
    );

    // arbitrary data to write into the output stream
    byte[] junkBytes = new byte[1024 * 1024];
    ThreadLocalRandom.current().nextBytes(junkBytes);

    S3AsyncClient asyncClient = S3AsyncClient.crtBuilder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .build();
    try (S3TransferManager transferManager = S3TransferManager.builder().s3Client(asyncClient).build()) {
      List<CompletableFuture<Void>> futures = new ArrayList<>();
      for (int i=0; i<threadCount; ++i) {
        ByteStreamer byteStreamer = new ByteStreamer(bucketName, transferManager, junkBytes);
        futures.add(
          CompletableFuture.runAsync(byteStreamer, executorService)
        );
      }
      futures.forEach(CompletableFuture::join);
    }
  }
}
