package com.example.itellouppgift;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileHandlerService {

    PaymentReceiver paymentReceiver;

    public String inputFile(MultipartFile file) throws FileInputException, IOException, ParseException {
            String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("_"));

        switch (fileType){
            case "_inbetalningstjansten.txt":
                typeInbetalningstjansten(file);
                break;

            case "_betalningsservice.txt":
                typeBetalningsservice(file);
                break;

            default:
                throw new FileInputException(file.getOriginalFilename() + " is of unknown type");
        }
        return file.getOriginalFilename() + " has been processed";
    }

    private void typeBetalningsservice(MultipartFile file) throws IOException, ParseException {
        boolean started = false;
        String tempLine;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1));
            while ((tempLine = reader.readLine()) != null){
                String post = new String(tempLine.getBytes("ISO-8859-1"), "UTF-8");

                switch (post.charAt(0)){
                    case 'O':
                        if (!started){
                            started = true;
                            String accountNumber = post.substring(1,16).replace(" ", "");
                            if (!accountNumber.matches("[0-9]+"))
                                throw new FileInputException(file.getOriginalFilename() + ", account number contains illegal characters");

                            Date paymentDate = new SimpleDateFormat("yyyyMMdd").parse(post.substring(40,48));
                            String currency = post.substring(48);

                            paymentReceiver.startPaymentBundle(accountNumber, paymentDate, currency);
                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " contains multiple starting posts");
                        }
                        break;
                    case 'B':
                        BigDecimal amount = new BigDecimal(post.substring(1,15).trim().replace(',', '.'));
                        String reference = post.substring(15).trim();

                        if (!reference.matches("[0-9A-ZÅÄÖ]+"))
                            throw new FileInputException(file.getOriginalFilename() + ", reference contains illegal characters");

                        paymentReceiver.payment(amount, reference);
                        break;
                    default:
                        throw new FileInputException(file.getOriginalFilename() + ", contains unknown type of post");
                }
            }

            paymentReceiver.endPaymentBundle();
        } catch (Exception e){
            throw e;
        }
    }

    private void typeInbetalningstjansten(MultipartFile file) throws IOException, ParseException {
        boolean started = false;
        String tempLine;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1));
            while ((tempLine = reader.readLine()) != null){
                String post = new String(tempLine.getBytes("ISO-8859-1"), "UTF-8");

                switch (post.substring(0,2)){
                    case "00":
                        if (!started){
                            started = true;
                            String accountNumber = post.substring(10,14) + post.substring(14,24);
                            if (!accountNumber.matches("[0-9]+"))
                                throw new FileInputException(file.getOriginalFilename() + ", account number contains illegal characters");

                            paymentReceiver.startPaymentBundle(accountNumber, new Date(), "SEK");
                        } else {
                            throw new FileInputException(file.getOriginalFilename() + " contains multiple starting posts");
                        }
                        break;
                    case "30":
                        BigDecimal amount = new BigDecimal(post.substring(2,20) + "." + post.substring(20,22));
                        String reference = post.substring(40).trim();

                        if (!reference.matches("[0-9A-ZÅÄÖ]+"))
                            throw new FileInputException(file.getOriginalFilename() + ", reference contains illegal characters");

                        paymentReceiver.payment(amount, reference);
                        break;
                    case "99":
                        if (reader.readLine() != null)
                            throw new FileInputException(file.getOriginalFilename() + ", contains posts after end-post");

                        paymentReceiver.endPaymentBundle();
                        break;
                    default:
                        throw new FileInputException(file.getOriginalFilename() + ", contains unknown type of post");
                }
            }
        } catch (Exception e){
            throw e;
        }
    }
}
