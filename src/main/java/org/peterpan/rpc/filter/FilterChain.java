package org.peterpan.rpc.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author PeterPan
 * @date 2023/8/21
 * @description 拦截器链
 */
public class FilterChain {


    private List<IFilter> IFilters = new ArrayList<>();

    public void addFilter(IFilter IFilter){
        IFilters.add(IFilter);
    }


    public void addFilter(List<Object> filters){
        for (Object filter : filters) {
            addFilter((IFilter) filter);
        }
    }
    public void doFilter(FilterData data){
        for (IFilter IFilter : IFilters) {
            IFilter.doFilter(data);
        }
    }
}
