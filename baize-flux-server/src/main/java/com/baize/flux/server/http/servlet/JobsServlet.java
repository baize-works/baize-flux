package com.baize.flux.server.http.servlet;

import com.baize.flux.server.http.FluxServlet;
import com.baize.flux.server.service.JobRestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * /api/v1/jobs 集合资源。
 */
public final class JobsServlet
        extends FluxServlet {

    private final JobRestService service;
    private final int maxRequestBytes;

    public JobsServlet(
            JobRestService service,
            int maxRequestBytes) {

        this.service = service;
        this.maxRequestBytes = maxRequestBytes;
    }

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
             {

        validateSubmitContentType(request);

                 try {
                     write(
                             response,
                             202,
                             service.submit(
                                     requestBody(
                                             request,
                                             maxRequestBytes)));
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }


    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
             {

        int page =
                intParameter(
                        request,
                        "page",
                        1);

        int pageSize =
                intParameter(
                        request,
                        "pageSize",
                        20);

                 try {
                     write(
                             response,
                             200,
                             service.jobs(
                                     request.getParameter(
                                             "status"),
                                     page,
                                     pageSize));
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
}