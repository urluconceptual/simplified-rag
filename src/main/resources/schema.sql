-- DROP TABLE IF EXISTS chunks;
-- DROP TABLE IF EXISTS documents;

CREATE TABLE IF NOT EXISTS documents (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_name   VARCHAR2(255)   NOT NULL,
    file_type   VARCHAR2(10)    NOT NULL,
    uploaded_at TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    content     CLOB            NOT NULL,
    CONSTRAINT chk_file_type    CHECK (file_type IN ('PDF', 'DOCX', 'TXT'))
);

CREATE TABLE IF NOT EXISTS chunks (
    id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_id NUMBER         NOT NULL,
    chunk_index NUMBER         NOT NULL,
    chunk_text  VARCHAR2(4000) NOT NULL,
    embedding   VECTOR(384, FLOAT32),
    CONSTRAINT fk_document FOREIGN KEY (document_id)
        REFERENCES documents(id) ON DELETE CASCADE
);