package com.unibuc.simplifiedrag.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OnnxLoader {
    private static final Logger log = LoggerFactory.getLogger(OnnxLoader.class);

    private final JdbcTemplate jdbc;

    public OnnxLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

//    Source1: https://blogs.oracle.com/machinelearning/use-our-prebuilt-onnx-model-now-available-for-embedding-generation-in-oracle-database-23ai
//    Source2: https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/import-onnx-models-oracle-ai-database-end-end-example.html
//    Source3: https://docs.oracle.com/en/database/oracle/oracle-database/26/vecse/sql-quick-start-using-vector-embedding-model-uploaded-database.html#VECSE-GUID-403EB84E-3047-4905-844C-BD4A8670B8A4

    @EventListener(ApplicationReadyEvent.class)
    public void loadOnnxModel() {
        // Check if model already loaded
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_mining_models WHERE model_name = 'ALL_MINILM_L12_V2'",
                Integer.class
        );

        if (count != null && count > 0) {
            log.info("ONNX model already loaded, skipping.");
            return;
        }

        log.info("Loading ONNX model into Oracle...");
        jdbc.execute("""
            DECLARE
                ONNX_MOD_FILE VARCHAR2(100) := 'all_MiniLM_L12_v2.onnx';
                MODNAME VARCHAR2(500);
                LOCATION_URI VARCHAR2(200) := 'https://adwc4pm.objectstorage.us-ashburn-1.oci.customer-oci.com/p/iPX9W0MZeRkwJKWdFmdJCemmN-iKAl_bFvNGYLW7YqIrw4kKsukL24J2q93Beb9S/n/adwc4pm/b/OML-ai-models/o/';
            BEGIN
                SELECT UPPER(REGEXP_SUBSTR(ONNX_MOD_FILE, '[^.]+')) INTO MODNAME FROM dual;
                BEGIN
                    DBMS_DATA_MINING.DROP_MODEL(model_name => MODNAME);
                EXCEPTION WHEN OTHERS THEN NULL;
                END;
                DBMS_CLOUD.GET_OBJECT(
                    credential_name => NULL,
                    directory_name  => 'DATA_PUMP_DIR',
                    object_uri      => LOCATION_URI || ONNX_MOD_FILE
                );
                DBMS_VECTOR.LOAD_ONNX_MODEL(
                    directory  => 'DATA_PUMP_DIR',
                    file_name  => ONNX_MOD_FILE,
                    model_name => MODNAME
                );
                DBMS_OUTPUT.PUT_LINE('Model loaded: ' || MODNAME);
            END;
        """);

        log.info("ONNX model loaded successfully.");
    }
}