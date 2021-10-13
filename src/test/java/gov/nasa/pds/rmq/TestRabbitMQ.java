// Copyright © 2021, California Institute of Technology ("Caltech").
// U.S. Government sponsorship acknowledged.
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// • Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
// • Redistributions must reproduce the above copyright notice, this list of
//   conditions and the following disclaimer in the documentation and/or other
//   materials provided with the distribution.
// • Neither the name of Caltech nor its operating division, the Jet Propulsion
//   Laboratory, nor the names of its contributors may be used to endorse or
//   promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package gov.nasa.pds.rmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import gov.nasa.pds.crawler.util.ExceptionUtils;


public class TestRabbitMQ
{
    private static final String QUEUE_NAME = "harvest";

    
    private static class HarvestConsumer extends DefaultConsumer
    {
        public HarvestConsumer(Channel channel)
        {
            super(channel);
        }
    
        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, 
                AMQP.BasicProperties properties, byte[] body) throws IOException
        {
            long deliveryTag = envelope.getDeliveryTag();
            String msg = new String(body);
            System.out.println(msg);

            getChannel().basicAck(deliveryTag, false);
        }
    }
    
    
    
    public static void main(String[] args) throws Exception
    {
        consume();
    }
    
    
    public static void publish() throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        Connection con = factory.newConnection();
        Channel channel = con.createChannel();
        channel.basicPublish("", QUEUE_NAME, null, "test".getBytes());
        
        channel.close();
        con.close();
    }


    public static void consume() throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        Connection con = factory.newConnection();
        Channel channel = con.createChannel();
        channel.basicQos(1);
        HarvestConsumer consumer = new HarvestConsumer(channel);
        
        try
        {
            channel.basicConsume(QUEUE_NAME, false, consumer);
        }
        catch(Exception ex)
        {
            System.out.println("[ERROR] " + ExceptionUtils.getMessage(ex));
            //channel.close();
            con.close();
        }
        
        System.out.println("Done.");
        
        //System.in.read();
        //channel.close();
        //con.close();
    }

}
