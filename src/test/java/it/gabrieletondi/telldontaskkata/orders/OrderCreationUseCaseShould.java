package it.gabrieletondi.telldontaskkata.orders;

import it.gabrieletondi.telldontaskkata.domain.Order;
import it.gabrieletondi.telldontaskkata.domain.OrderItem;
import it.gabrieletondi.telldontaskkata.domain.OrderStatus;
import it.gabrieletondi.telldontaskkata.doubles.TestOrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class OrderCreationUseCaseShould {
    private final TestOrderRepository orderRepository = new TestOrderRepository();
    private final OrderCreationUseCase useCase = new OrderCreationUseCase(orderRepository, OrderObjectMother.productCatalog);

    @Test
    public void sellMultipleItems() {
        SellItemRequest saladRequest = new SellItemRequest();
        saladRequest.setProductName("salad");
        saladRequest.setQuantity(2);

        SellItemRequest tomatoRequest = new SellItemRequest();
        tomatoRequest.setProductName("tomato");
        tomatoRequest.setQuantity(3);

        final SellItemsRequest request = new SellItemsRequest();
        request.setRequests(new ArrayList<>());
        request.getRequests().add(saladRequest);
        request.getRequests().add(tomatoRequest);

        useCase.run(request);

        final Order insertedOrder = orderRepository.getSavedOrder();
        assertThat(insertedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(insertedOrder.getTotal()).isEqualTo(new BigDecimal("23.20"));
        assertThat(insertedOrder.getTax()).isEqualTo(new BigDecimal("2.13"));
        assertThat(insertedOrder.getCurrency()).isEqualTo("EUR");
        assertThat(insertedOrder.getItems()).hasSize(2);

        final OrderItem saladItem = insertedOrder.getItems().get(0);
        assertThat(saladItem.getProduct().getName()).isEqualTo("salad");
        assertThat(saladItem.getProduct().getPrice()).isEqualTo(new BigDecimal("3.56"));
        assertThat(saladItem.getQuantity()).isEqualTo(2);
        assertThat(saladItem.getTaxedAmount()).isEqualTo(new BigDecimal("7.84"));
        assertThat(saladItem.getTax()).isEqualTo(new BigDecimal("0.72"));

        final OrderItem tomatoItem = insertedOrder.getItems().get(1);
        assertThat(tomatoItem.getProduct().getName()).isEqualTo("tomato");
        assertThat(tomatoItem.getProduct().getPrice()).isEqualTo(new BigDecimal("4.65"));
        assertThat(tomatoItem.getQuantity()).isEqualTo(3);
        assertThat(tomatoItem.getTaxedAmount()).isEqualTo(new BigDecimal("15.36"));
        assertThat(tomatoItem.getTax()).isEqualTo(new BigDecimal("1.41"));
    }

    @Test
    public void unknownProduct() {
        SellItemsRequest request = new SellItemsRequest();
        request.setRequests(new ArrayList<>());
        SellItemRequest unknownProductRequest = new SellItemRequest();
        unknownProductRequest.setProductName("unknown product");
        request.getRequests().add(unknownProductRequest);

        assertThatExceptionOfType(UnknownProductException.class)
                .isThrownBy( () -> useCase.run(request));
    }
}
