package org.example.services;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.mindee.MindeeClientV2;
import com.mindee.InferenceParameters;
import com.mindee.input.LocalInputSource;
import com.mindee.parsing.v2.InferenceResponse;
import com.mindee.parsing.v2.field.InferenceFields;
import org.example.entities.ReceiptData;

import java.io.File;
import java.io.IOException;
public class MindeeOcrService {

    private final MindeeClientV2 client;
    private final String modelId;

    public MindeeOcrService(String apiKey, String modelId) {
        this.client = new MindeeClientV2(apiKey);
        this.modelId = modelId;
    }

    public ReceiptData scan(String filePath) throws Exception {

        InferenceParameters params = InferenceParameters.builder(modelId).build();
        LocalInputSource input = new LocalInputSource(new File(filePath));

        InferenceResponse response =
                client.enqueueAndGetInference(input, params);

        InferenceFields fields =
                response.getInference().getResult().getFields();

        ReceiptData data = new ReceiptData();

        data.supplierName =
                fields.getSimpleField("supplier_name").getStringValue();

        data.date =
                fields.getSimpleField("date").getStringValue();

        data.totalAmount =
                fields.getSimpleField("total_amount").getDoubleValue();

        data.currency = "TND"; // pour l'instant simple

        return data;
    }
}