package org.peterpan.rpc.filter;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description 拦截器
 */
public interface IFilter {

    void doFilter(FilterData filterData);


}
