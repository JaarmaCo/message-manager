package com.github.jaarmaco.messagemanager.network;

import org.springframework.stereotype.Service;

import com.github.jaarmaco.messagemanager.lib.functional.Result;
import com.github.jaarmaco.messagemanager.messages.model.Message;

@Service
public class NetworkService {

    public Result<Integer> forwardMessage(Message message) {
        return Result.of(0);
    }
}

