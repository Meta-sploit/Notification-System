package com.taskmanagement.config;

import com.taskmanagement.event.NotificationMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.notification.kafka.topic.task-events:task-events}")
    private String taskEventsTopic;

    @Value("${app.notification.kafka.topic.notifications:notifications}")
    private String notificationsTopic;

    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name(taskEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

