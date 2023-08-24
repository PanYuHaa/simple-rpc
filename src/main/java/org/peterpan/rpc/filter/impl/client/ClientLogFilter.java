package org.peterpan.rpc.filter.impl.client;

import org.peterpan.rpc.filter.FilterData;
import org.peterpan.rpc.filter.IClientBeforeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author PeterPan
 * @date 2023/8/21
 * @description 日志
 */
public class ClientLogFilter implements IClientBeforeFilter {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);

    @Override
    public void doFilter(FilterData filterData) {
        logger.info(filterData.toString());
    }
}
