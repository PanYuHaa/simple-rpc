package rpc.peterpan.com.IDL.Hello;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author PeterPan
 * @date 2023/7/11
 * @description
 */
@Data
@AllArgsConstructor
public class HelloRequest implements Serializable {
    private String name;
}