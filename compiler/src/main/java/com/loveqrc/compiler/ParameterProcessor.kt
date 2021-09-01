package com.loveqrc.compiler

import com.google.auto.service.AutoService
import com.loveqrc.annotation.Parameter
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import com.squareup.javapoet.TypeSpec

import com.squareup.javapoet.JavaFile
import javax.lang.model.element.Modifier


//注册注解处理器 Processor::class
@AutoService(Processor::class)
//注册需要处理哪个注解
@SupportedAnnotationTypes("com.loveqrc.annotation.Parameter")
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//定义处理器接收参数的可以
// 在build.gradle文件中配置
// android {
//    defaultConfig {
//        kapt {
//            arguments {
//                arg("content", project.getName())
//            }
//        }
//    }
//    }
//
@SupportedOptions("content")

class ParameterProcessor : AbstractProcessor() {
    // 操作Element工具类 (类、函数、属性都是Element)
    lateinit var elementUtils: Elements

    //type(类信息)工具类，包含用于操作TypeMirror的工具方法
    lateinit var typeUtils: Types

    // Messager用来报告错误，警告和其他提示信息
    lateinit var messager: Messager

    // 文件生成器 类/资源，Filter用来创建新的源文件，class文件以及辅助文件
    lateinit var filer: Filer

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private val tempParameterMap: HashMap<TypeElement, MutableList<Element>> = HashMap()


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        messager = processingEnv.messager
        filer = processingEnv.filer

        val content: String? = processingEnv.options["content"]
        messager.printMessage(Diagnostic.Kind.NOTE, content ?: "content is null")
    }

    /**
     * 处理注解的入口，相当于main函数
     *
     * @param annotations 因为注解处理器可以处理多个注解，所以这里返回的是数组
     * @param roundEnv 当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (annotations.isEmpty()) {
            return false
        }
        //获取被@Parameter注解的元素(属性)
        val elements = roundEnv.getElementsAnnotatedWith(Parameter::class.java)

        if (elements.isEmpty()) {
            return false
        }
        //获取注解的值,将其存起来
        valueOfParameterMap(elements)
        //生成对应的类文件
        createParameterFile()

        return true
    }

    private fun createParameterFile() {
        if (tempParameterMap.isEmpty()) {
            return
        }
        //通过Element工具类，获取对应的类型
        val activityType = elementUtils.getTypeElement(Constants.ACTIVITY) as TypeElement
        //获取要实现的接口的类型
        val parameterType = elementUtils.getTypeElement(Constants.PARAMETER_LOAD) as TypeElement

        //  配置loadParameter方法的(Object target)参数
        val parameterSpec: ParameterSpec =
            ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAME).build()

        //每一个entry对应一个Activity
        tempParameterMap.entries.forEach {
            val typeElement = it.key
            //如果不是在Activity中使用，那么就没意义了
            if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                throw RuntimeException("@Parameter only support for activity")
            }

            //获取类名
            val className = ClassName.get(typeElement)

            // 方法体内容构建
            val factory = ParameterFactory.Builder(parameterSpec)
                .setMessager(messager)
                .setClassName(className)
                .build()

            // 添加方法体内容的第一行
            //MainActivity t = (MainActivity) target;
            factory.addFirstStatement()

            // 遍历类里面所有属性
            for (fieldElement in it.value) {
                factory.buildStatement(fieldElement)
            }


            // 最终生成的类文件名（类名Parameter）
            val finalClassName = typeElement.simpleName.toString() + Constants.PARAMETER_FILE_NAME
            messager.printMessage(
                Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                        className.packageName() + "." + finalClassName
            )

            // MainActivity$$Parameter
            JavaFile.builder(
                className.packageName(),  // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                    .addSuperinterface(ClassName.get(parameterType)) // 实现ParameterLoad接口
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                    .build()
            ) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer) // 文件生成器开始生成类文件


        }

    }

    private fun valueOfParameterMap(elements: MutableSet<out Element>) {
        elements.forEach {
            messager.printMessage(Diagnostic.Kind.NOTE, it.simpleName)
            //获取当前element的父类
            val enclosingElement = it.enclosingElement as TypeElement
            // example:
            //class MainActivity : AppCompatActivity() {
            //    @Parameter
            //    var user: String? = null
            // }

            // it : user  VariableElement
            // enclosingElement : MainActivity  TypeElement
            messager.printMessage(Diagnostic.Kind.NOTE, enclosingElement.simpleName)

            if (tempParameterMap.containsKey(enclosingElement)) {
                tempParameterMap[enclosingElement]!!.add(it)
            } else {
                val fields = ArrayList<Element>()
                fields.add(it)
                tempParameterMap[enclosingElement] = fields
            }
        }
    }
}