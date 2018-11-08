package com.meltmedia.cadmium.servlets;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * This is the top level exception.
 */
public class InvalidURIException extends Exception
{
    /**
     * Creates a new invalid uri exception with a message and a cause.
     */
    public InvalidURIException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Creates a new invalid uri exception with just a message.
     */
    public InvalidURIException( String message )
    {
        super( message );
    }

    /**
     * Creates a new invalid uri exception with just a cause.
     */
    public InvalidURIException( Throwable cause )
    {
        super( cause );
    }

    /**
     * Formats an Exception message with request information
     *
     * @param message Message to start the exception.
     * @param request request to get information from.
     * @return
     */
    public static String getRequestInformationMessage(String message, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(message).append('\n');
        sb.append("Request Information - ").append('\n');
        sb.append("Request URL: " + request.getRequestURL().toString()).append('\n');
        sb.append("Request Method: " + request.getMethod()).append('\n');
        sb.append("Request AuthType: " + request.getAuthType()).append('\n');

        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            sb.append("Request Headers - ").append('\n');
            while (headerNames.hasMoreElements()) {
                sb.append("Header: " + request.getHeader(headerNames.nextElement())).append('\n');
            }
        }

        return sb.toString();
    }
}