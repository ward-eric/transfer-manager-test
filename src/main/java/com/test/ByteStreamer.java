package com.test;

import org.slf4j.Logger;
import software.amazon.awssdk.core.async.BlockingOutputStreamAsyncRequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;

import java.time.Duration;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;

import static org.slf4j.LoggerFactory.getLogger;

public class ByteStreamer implements Runnable {
  private static final Logger LOG = getLogger(ByteStreamer.class);

  private String bucketName;
  private S3TransferManager transferManager;
  private byte[] junkBytes;

  public ByteStreamer(String bucketName, S3TransferManager transferManager, byte[] junkBytes) {
    this.bucketName = bucketName;
    this.transferManager = transferManager;
    this.junkBytes = junkBytes;
  }

  @Override
  public void run() {
    String key = "testing/" + UUID.randomUUID();
    LOG.info("Starting upload for {}", key);
    BlockingOutputStreamAsyncRequestBody body = BlockingOutputStreamAsyncRequestBody.builder()
      .contentLength(null)
      .subscribeTimeout(Duration.ofSeconds(30))
      .build();
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build();
    Upload upload = transferManager.upload(UploadRequest.builder()
      .putObjectRequest(putObjectRequest)
      .requestBody(body)
      .build());
    try (DeflaterOutputStream outputStream = new DeflaterOutputStream(body.outputStream())) {
      for (int i=0; i<100; ++i) {
        outputStream.write(junkBytes);
      }
      outputStream.flush();
      LOG.info("Completed upload for {}", key);
    } catch (Exception e) {
      LOG.error("Failed upload for {}", key, e);
      throw new RuntimeException(e);
    }
    upload.completionFuture().join();
  }
}
