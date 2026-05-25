package com.amazon.service_a.payments.infrastructure;

import com.amazon.service_a.payments.aplication.GetPaymentUseCase;
import com.amazon.service_a.payments.infrastructure.dto.PaymentDtoMapper;
import com.amazon.service_a.payments.infrastructure.dto.PaymentResponse;
import com.amazon.service_a.shared.infrastructure.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
/* TODO el paquete payments no es lo correcto. Para el microservicio un paymente sin order no deberia de existir por lo tanto
 * no tiene esa identidad para permanecer fuera del orders. Todo cambio de payment se tiene que hacer a traves de un order
 * Reestrutura esta parte
*/
public class PaymentController {

    private final GetPaymentUseCase getUseCase;

    public PaymentController(GetPaymentUseCase getUseCase) {
        this.getUseCase = getUseCase;
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> get(@PathVariable Long id) {
        PaymentResponse payment = PaymentDtoMapper.toResponse(getUseCase.getPayment(id));
        
        return ApiResponse.ok("Payment retrieved successfully", payment);
    }
}
