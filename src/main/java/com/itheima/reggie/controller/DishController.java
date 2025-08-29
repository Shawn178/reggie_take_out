package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*
菜品管理Controller类：
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    // 自动注入DishService、DishFlavorService
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    // 自动注入CategoryService，为了获取分类对象，从而获取分类名称
    @Autowired
    private CategoryService categoryService;

    /*
    一、新增菜品的方法

    实体类Dish中未提及的属性：flavor，因此无法仅封装Dish对象就获取到前端返回的flavors
    1.R 类无法被解析（缺少import）
    2.需要处理菜品和菜品口味的关联关系
    3.前端会同时传递菜品信息和口味信息

    解决办法：推荐使用DTO类(数据传输对象，一般用于展示层与服务层之间的数据传递)
    封装到DTO类中，包含Dish类的全部属性，以及新增的flavors属性

    因为前端传输的是JSON数据，所以需要@RequestBody注解
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        /*
        调试期间遇到的各种错误汇总：
        1.调试期间，flavor参数为空，菜品保存成功，但是口味数据没有保存成功
          原因：DishDto 类缺少 @Data 注解。没有这个注解，Spring Boot无法通过反射访问私有字段，导致 flavors 属性无法被正确绑定。
        2.调试中加了@Data，但是保存失败
          原因：方法中忘记返回值了，返回null肯定失败
         */
        log.info("新值菜品：{}",dishDto.toString());

        // 调用Service层的新增菜品和口味的方法
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }


    /*
    二、分页查询的方法：直接调用之前写好的
    page:页码；pageSize:每页显示的条数

    首先不推荐这种写法！
      1.核心瓶颈在于 N+1 查询：每条 Dish 都单独 getById(categoryId) 查一次分类，页大小为 N 就是 N 次额外查询，延迟和数据库压力都会放大。
      2.Stream 只是写法风格问题，不是性能根源；真正影响性能的是查询策略。
     */

     /*
      更高效率的主流替代方案：批量查询 + 内存映射

            @GetMapping("/page")
      public R<Page> pageEfficient(int page, int pageSize, String name) {
          // 1. 分页查询 Dish（一次查询）
          Page<Dish> pageInfo = new Page<>(page, pageSize);
          LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
          queryWrapper.like(name != null, Dish::getName, name);
          queryWrapper.orderByDesc(Dish::getUpdateTime);
          dishService.page(pageInfo, queryWrapper);

          // 2. 取 records 并提取去重的 categoryId
          List<Dish> records = pageInfo.getRecords();
          Set<Long> categoryIds = records.stream()
                  .map(Dish::getCategoryId)
                  .filter(id -> id != null)
                  .collect(Collectors.toSet());

          // 3. 批量查询所有 Category（第二次查询）
          List<Category> categories = categoryIds.isEmpty()
                  ? java.util.Collections.emptyList()
                  : categoryService.list(
                          new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                                  .in(Category::getId, categoryIds)
                    );

          // 4. 构建内存映射：categoryId -> categoryName
          java.util.Map<Long, String> categoryIdToName = categories.stream()
                  .collect(java.util.stream.Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

          // 5. 转换 List<Dish> -> List<DishDto> 并补齐 categoryName
          List<DishDto> list = records.stream().map(item -> {
              DishDto dto = new DishDto();
              org.springframework.beans.BeanUtils.copyProperties(item, dto);

              Long cid = item.getCategoryId();
              String cname = cid == null ? null : categoryIdToName.get(cid);
              dto.setCategoryName(cname);

              return dto;
          }).collect(java.util.stream.Collectors.toList());

          // 6. 拷贝分页元信息，并设置新 records
          Page<DishDto> dishDtoPage = new Page<>();
          org.springframework.beans.BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
          dishDtoPage.setRecords(list);

          return R.success(dishDtoPage);
      }
      */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        // 构造一个分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
          // 构造DishDto的分页构造器
        Page<DishDto> dishDtoPage = new Page<>();

        // 构造一个条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件：根据name查询,like是模糊查询的方式
        queryWrapper.like(name != null,Dish::getName,name);
        // 添加排序条件：
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 最后调用Service层的分页查询方法,把查询的值给pageInfo对象
        dishService.page(pageInfo,queryWrapper);
        /*
         把pageInfo的值拷贝给DishDto对象！！
         使用BeanUtils.copyProperties()方法，但是不需要拷贝Page的records列表，直接ignoreProperties忽略
         第三个参数 "records" 表示不复制记录列表。原因是 pageInfo 中的记录是 List<Dish>，而我们最终需要的是 List<DishDto>。这两者类型不同，不能直接复制。必须等下“手动把每条 Dish 转成 DishDto”后再设置回去。


         还要对records处理：
         第一步把当前页的“数据行”取出来，准备做“逐条转换”：
             对每个 Dish，查询它的分类名称，把 categoryId → categoryName 填充到 DishDto。
          第二步：把 records 通过 stream/map 或 for 循环转为 List<DishDto> 并设置到 dishDtoPage.setRecords(...)。

          一句话总括：
          先复制分页“元信息”，再把 List<Dish> 逐条转换为 List<DishDto>（补上 categoryName 等展示字段），最后把新列表塞回 Page<DishDto> 并返回。这样既保证分页信息正确，又让每条记录满足前端展示需求。
         */

        // 1.拷贝分页“壳子”，暂不拷贝列表
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        // 2.取出原始“数据行”
        List<Dish> records = pageInfo.getRecords();

        // 3.把每个 Dish 转成 DishDto，并补齐分类名
        List<DishDto> list = records.stream().map((item) -> {
            // 先创建一个Dto对象
            DishDto dishDto = new DishDto();
            // 把全部的dish属性通过item先复制给DishDto，然后单独再修改分类名称
            BeanUtils.copyProperties(item, dishDto);

            // 根据菜品对象，获取菜品分类id，item现在代表Dish菜品
            Long categoryId = item.getCategoryId();

            // 拿到分类Id，去查分类表，获取分类名称(分类名称在Category表中)
            // 注入CategoryService，根据分类id获取分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                // 再从获取的category对象中获取分类名称
                String categoryName = category.getName();

                // 把获取的分类名称复制到dishDto中
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        /*
        4.把新的列表塞回分页对象
         最后不能直接返回R.success(pageInfo),因为Dish类的菜品分类是id，而前段需要的是分类名称
         而DishDto类中有菜品分类CategoryName属性，而且DishDto继承自Dish类
         所以把遍历后的list集合赋给dishDtoPage
         */
        dishDtoPage.setRecords(list);

        // 5.返回给前端
        return R.success(dishDtoPage);
    }


    /*
    三、根据id查询菜品信息和对应的口味信息
    开发修改菜品功能，其实就是在服务端编写代码去处理前端页面发送的这4次请求即可。

     前端返回数据有各种String字符串，还包括口味列表，所以要封装一个DishDto类
     且前端返回的id是Restful风格，要使用@PathVariable注解
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        // 调用Service层的方法即可和数据库交互
        // 先在对应service层写一个get方法，然后在对应Impl类写一个实现的方法
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /*
    四、修改菜品
    因为前端返回是JSON数据，所以要@RequestBody注解
    因为要修改菜品的同时修改菜品的口味，所以要@Transactional事务注解
     */
    @PutMapping
    public R<String> updateWithFlavor(@RequestBody DishDto dishDto){
        log.info("修改菜品:{}",dishDto);

        // 调用Service层的修改方法，同时更新菜品和口味信息
        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }


    /*
    五、批量删除菜品
    开发批量删除菜品功能，其实就是在服务端编写代码去处理前端页面发送的这1次请求即可。
    前端返回的是一个id数组，所以要使用@RequestParam注解


    调试期间错误：
    1.写成@DeleteMapping("/{ids}"),导致405错误，前端发送的是 DELETE 请求到 /dish 端点，但我Controller中@DeleteMapping("/{ids}") 路径是 /dish/{ids}
    修改方法：直接写成@DeleteMapping，因为类直接获取dish了

    2.忘记添加@RequestParam注解，报错500，因为前端发送的是一个数组，所以要使用@RequestParam注解
    修改方法：添加@RequestParam注解
     */
     @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
         // 加日志
         log.info("删除菜品:{}",ids);
         // 调用Service层的删除方法
         dishService.deleteBatch(ids);
         return R.success("删除成功");
     }


     /*
     六、批量停售、起售菜品

     调试错误汇总：
     1.404 错误的根本原因：
       Controller 中 @PostMapping("/status") 只匹配 /dish/status；但前端请求的是 /dish/status/0
       修改方法：写成@PostMapping("/status/{status}")
    2.500错误：
      路径参数 {status} 必须用 @PathVariable 获取
      查询参数 ?ids=... 必须用 @RequestParam 获取
      */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        // 加日志
        log.info("菜品状态：{}",status);

        // 调用Service层的批量起售、停售方法
        dishService.updateStatus(status,ids);

        return R.success("更新售卖状态成功");
    }



    /*
    七、根据条件查询对应的菜品数据

    调试遇到错误：
    1.加断点后点击套餐管理，不回调试界面，且没办法调试
    解决办法：加@ModelAttribute；因为前端：?categoryId=123&name=xxx → dish.categoryId=123, dish.name=xxx
    @ModelAttribute Dish dish 会自动：
      创建 Dish 对象,将 categoryId=1958376345645883393 绑定到 dish.categoryI,其他属性为 null
     */
    /*@GetMapping("/list")
    public R<List<Dish>> list(@ModelAttribute Dish dish){
        // 构造一个查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件:根据分类id(categoryId)查找菜品
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        log.info("添加分类ID查询条件：categoryId = {}", dish.getCategoryId());
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        
        // 全部查询，无论在售、停售，不加查询状态的条件！
        // queryWrapper.eq(Dish::getStatus,1);

        // 调用Service层修改数据库
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/



    /*
    对根据条件查询菜品的方法 改造一下
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(@ModelAttribute Dish dish){
        // 构造一个查询条件对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件:根据分类id(categoryId)查找菜品
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        log.info("添加分类ID查询条件：categoryId = {}", dish.getCategoryId());
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 全部查询，无论在售、停售，不加查询状态的条件！
        // queryWrapper.eq(Dish::getStatus,1);

        // 调用Service层修改数据库
        List<Dish> list = dishService.list(queryWrapper);

        // 3.把每个 Dish 转成 DishDto，并补齐分类名
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            // 先创建一个Dto对象
            DishDto dishDto = new DishDto();
            // 把全部的dish属性通过item先复制给DishDto，然后单独再修改分类名称
            BeanUtils.copyProperties(item, dishDto);

            // 根据菜品对象，获取菜品分类id，item现在代表Dish菜品
            Long categoryId = item.getCategoryId();

            // 拿到分类Id，去查分类表，获取分类名称(分类名称在Category表中)
            // 注入CategoryService，根据分类id获取分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                // 再从获取的category对象中获取分类名称
                String categoryName = category.getName();

                // 把获取的分类名称复制到dishDto中
                dishDto.setCategoryName(categoryName);
            }

            // 再获取菜品的口味信息，可以直接调用函数getByIdWithFlavor()
            Long dishId = item.getId(); // 从当前菜品对象 item 中获取菜品的ID
            DishDto idWithFlavor = dishService.getByIdWithFlavor(dishId);
            // 再把获取的口味信息复制到dishDto中
            BeanUtils.copyProperties(idWithFlavor,dishDto);

            return dishDto;
        }).collect(Collectors.toList());





        return R.success(dishDtoList);
    }
    
}
