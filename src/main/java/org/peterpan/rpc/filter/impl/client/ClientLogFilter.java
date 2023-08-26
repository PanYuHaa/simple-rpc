package org.peterpan.rpc.filter.impl.client;

import lombok.extern.slf4j.Slf4j;
import org.peterpan.rpc.filter.FilterData;
import org.peterpan.rpc.filter.IClientBeforeFilter;


/**
 * @author PeterPan
 * @date 2023/8/21
 * @description 日志
 */
@Slf4j
public class ClientLogFilter implements IClientBeforeFilter {

    @Override
    public void doFilter(FilterData filterData) {
        log.info(filterData.toString());
    }
}
