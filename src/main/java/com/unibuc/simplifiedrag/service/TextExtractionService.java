package com.unibuc.simplifiedrag.service;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import java.io.InputStream;

//Source: https://www.baeldung.com/apache-tika
@Service
public class TextExtractionService {
    private static final Logger log = LoggerFactory.getLogger(TextExtractionService.class);

    public String extract(MultipartFile file) throws Exception {
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (TikaInputStream tis = TikaInputStream.get(file.getBytes())) {
            parser.parse(tis, handler, metadata, context);
        }

        String text = handler.toString().strip();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not extract any text from the file.");
        }

        log.info("Extracted {} characters from {}",
                text.length(), file.getOriginalFilename());
        return text;
    }
}