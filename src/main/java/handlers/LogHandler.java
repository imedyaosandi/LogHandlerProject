package handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

public class LogHandler extends AbstractSynapseHandler{
    private static final Log log = LogFactory.getLog(LogHandler.class);

    public boolean handleRequestInFlow(MessageContext messageContext) {
        messageContext.setProperty(HandlerConstants.REQUEST_IN_TIME, System.currentTimeMillis());
        messageContext.setProperty(HandlerConstants.FLOW_DIRECTION,"Request-In-Flow");
        logResponse(messageContext);
        return true;
    }

    public boolean handleRequestOutFlow(MessageContext messageContext) {
        messageContext.setProperty(HandlerConstants.FLOW_DIRECTION,"Request-Out-Flow");
        if(messageContext.getProperty("SYNAPSE_REST_API") != null){
            messageContext.setProperty(HandlerConstants.SERVICE_TYPE,"API");
            messageContext.setProperty(HandlerConstants.SERVICE_NAME,messageContext.getProperty("SYNAPSE_REST_API"));
            if(messageContext.getProperty("REST_FULL_REQUEST_PATH")!=null){
                messageContext.setProperty(HandlerConstants.RESOURCE,messageContext.getProperty("REST_FULL_REQUEST_PATH"));
            }
        }
        else{
            messageContext.setProperty(HandlerConstants.SERVICE_TYPE,"PROXY_SERVICE");
            messageContext.setProperty(HandlerConstants.SERVICE_NAME,messageContext.getProperty("proxy.name"));
        }

        messageContext.setProperty(HandlerConstants.BACKEND_REQUEST_START_TIME, System.currentTimeMillis());
        long requestLatency=getRequestLatency(messageContext);
        logResponse(messageContext);
        return true;
    }

    public boolean handleResponseInFlow(MessageContext messageContext) {
        messageContext.setProperty(HandlerConstants.FLOW_DIRECTION,"Response-In-Flow");
        messageContext.setProperty(HandlerConstants.RESPONSE_IN_TIME, System.currentTimeMillis());
        long beTotalLatency = getBackendLatency(messageContext);
        //log.info("BACKEND_LATENCY: " + beTotalLatency);
        logResponse(messageContext);
        return true;
    }

    public boolean handleResponseOutFlow(MessageContext messageContext) {
        messageContext.setProperty(HandlerConstants.FLOW_DIRECTION,"Response-Out-Flow");
        long responseLatency=getResponseLatency(messageContext);
        getRestHttpResponseStatusCode(messageContext);
        getRestHttpResponseStatusMessage(messageContext);
        logResponse(messageContext);
        return true;
    }

    private long getRequestLatency(MessageContext messageContext) {
        long requestLatency=0;
        long requestInTime=0;
        long requestOutTime=0;
        try{
            requestInTime=Long.parseLong(String.valueOf(messageContext.getProperty(HandlerConstants.REQUEST_IN_TIME)));
            requestLatency=System.currentTimeMillis()-requestInTime;
            messageContext.setProperty(HandlerConstants.REQUEST_MEDIATION_LATENCY,requestLatency);
        }catch (Exception e){
            log.error("Error getRequestLatency -  " + e.getMessage(), e);
        }

        return requestLatency;

    }
    private long getResponseLatency(MessageContext messageContext) {
        long responseLatency=0;
        long responseInTime=0;
        long requestOutTime=0;
        try{
            responseInTime=Long.parseLong(String.valueOf(messageContext.getProperty(HandlerConstants.RESPONSE_IN_TIME)));
            responseLatency=System.currentTimeMillis() - responseInTime;
            messageContext.setProperty(HandlerConstants.RESPONSE_MEDIATION_LATENCY,responseLatency);
        } catch (Exception e){
            log.error("Error getResponseLatency -  " + e.getMessage(), e);
        }
        return responseLatency;
    }

    private long getBackendLatency(org.apache.synapse.MessageContext messageContext) {
        long beTotalLatency = 0;
        //long beStartTime = 0;
        //long beEndTime = 0;
        long executionStartTime = 0;
        try {
            executionStartTime = Long.parseLong(String.valueOf(messageContext.getProperty(HandlerConstants.BACKEND_REQUEST_START_TIME)));
            beTotalLatency=System.currentTimeMillis() - executionStartTime;
            messageContext.setProperty(HandlerConstants.BACKEND_LATENCY, beTotalLatency);
        } catch (Exception e) {
            log.error("Error getBackendLatency -  " + e.getMessage(), e);
        }
        return beTotalLatency;
    }

    private static void getRestHttpResponseStatusCode(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        messageContext.setProperty(HandlerConstants.HTTP_STATUS_CODE,String.valueOf(axis2MsgContext.getProperty("HTTP_SC")));
    }
    private static void getRestHttpResponseStatusMessage(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        messageContext.setProperty(HandlerConstants.HTTP_STATUS_MESSAGE,String.valueOf(axis2MsgContext.getProperty("HTTP_SC_DESC")));
    }
    private static void logResponse(org.apache.synapse.MessageContext messageContext){
        log.info("FLOW_DIRECTION: "+messageContext.getProperty(HandlerConstants.FLOW_DIRECTION)
                + ", SERVICE_TYPE: "+messageContext.getProperty(HandlerConstants.SERVICE_TYPE)
                + ", SERVICE_NAME: "+messageContext.getProperty(HandlerConstants.SERVICE_NAME)
                +", RESOURCE: "+messageContext.getProperty(HandlerConstants.RESOURCE)
                +", REQUEST_MEDIATION_LATENCY: "+ messageContext.getProperty(HandlerConstants.REQUEST_MEDIATION_LATENCY)
        +", BACKEND_LATENCY: "+ messageContext.getProperty(HandlerConstants.BACKEND_LATENCY)
        +", RESPONSE_MEDIATION_LATENCY: "+messageContext.getProperty(HandlerConstants.RESPONSE_MEDIATION_LATENCY)
        +", "+HandlerConstants.HTTP_STATUS_CODE+": "+messageContext.getProperty(HandlerConstants.HTTP_STATUS_CODE)
        +", "+HandlerConstants.HTTP_STATUS_MESSAGE+": "+messageContext.getProperty(HandlerConstants.HTTP_STATUS_MESSAGE)
        +", "+HandlerConstants.ERROR_CODE+": "+messageContext.getProperty(HandlerConstants.ERROR_CODE)
        +", "+HandlerConstants.ERROR_MESSAGE+": "+messageContext.getProperty(HandlerConstants.ERROR_MESSAGE));
    }
}
