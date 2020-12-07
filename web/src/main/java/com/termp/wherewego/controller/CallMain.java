package com.termp.wherewego.controller;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CallMain {
    public static String mainfunc(String num1, String num2, String num3)  {
        System.out.println("Python Call");
        String[] command = new String[5];
        String result = null;
        command[0] = "python";
        //command[1] = "\\workspace\\java-call-python\\src\\main\\resources\\test.py";
        command[1] = "C:/Users/jaehun/Desktop/git/where-to-go/web/src/main/clt/test.py";
        command[2] = num1;
        command[3] = num2;
        command[4] = num3;
        try {
            result =  execPython(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String execPython(String[] command) throws IOException, InterruptedException {
        CommandLine commandLine = CommandLine.parse(command[0]);
        for (int i = 1; i< command.length; i++) {
            commandLine.addArgument(command[i]);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);
        DefaultExecutor executor = new DefaultExecutor();
        System.out.println(commandLine.toString());
        executor.setStreamHandler(pumpStreamHandler);
        int result = executor.execute(commandLine);
        System.out.println("result: " + result);
        System.out.println("output: " + new String(outputStream.toByteArray(),"MS949"));
        return new String(outputStream.toByteArray(),"MS949");
    }

}