package io.namjune.basicrestapi.accounts;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

//@JsonComponent 로 등록하면 Account 가 serialize 되는 모든 곳에서 사용되는데, 불필요할 수 있다. account 수정할 때는 정보를 받아야 된다.
//@JsonComponent
// 따라서, JsonComponent로 사용하지 않고, id 만 serialize 해서 사용할 곳에서만 @JsonSerialize(using = AccountSerializer.class) 형태로 사용하면 된다.
public class AccountSerializer extends JsonSerializer<Account> {

    @Override
    public void serialize(Account account, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", account.getId());
        gen.writeEndObject();
    }
}
