package com.flowiee.app.controller;

import com.flowiee.app.entity.Category;
import com.flowiee.app.service.CategoryService;
import com.flowiee.app.security.author.ValidateModuleStorage;
import com.flowiee.app.entity.DocData;
import com.flowiee.app.entity.DocField;
import com.flowiee.app.entity.Document;
import com.flowiee.app.dto.DocMetaDTO;
import com.flowiee.app.service.DocDataService;
import com.flowiee.app.service.DocFieldService;
import com.flowiee.app.service.DocShareService;
import com.flowiee.app.service.DocumentService;
import com.flowiee.app.service.FileStorageService;
import com.flowiee.app.service.AccountService;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.base.BaseController;

import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.FlowieeUtil;
import com.flowiee.app.utils.PagesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@CrossOrigin
@Controller
@RequestMapping("/storage")
public class DocumentController extends BaseController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private DocFieldService docFieldService;
    @Autowired
    private DocDataService docDataService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private DocShareService docShareService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ValidateModuleStorage validateModuleStorage;

    //Dashboard
    @GetMapping("/dashboard")
    public ModelAndView showDashboardOfSTG() {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.SYS_LOGIN);
        }
        if (validateModuleStorage.dashboard()) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.STG_DASHBOARD);
            //Loại tài liệu
            List<Category> listLoaiTaiLieu = categoryService.findSubCategory(AppConstants.DOCUMENTTYPE);
            List<String> listTenOfDocType = new ArrayList<>();
            List<Integer> listSoLuongOfDocType = new ArrayList<>();
            for (Category docType : listLoaiTaiLieu) {
                listTenOfDocType.add(docType.getName());
                listSoLuongOfDocType.add(docType.getListDocument() != null ? docType.getListDocument().size() : 0);
            }
            modelAndView.addObject("reportOfDocType_listTen", listTenOfDocType);
            modelAndView.addObject("reportOfDocType_listSoLuong", listSoLuongOfDocType);            
            return baseView(modelAndView);
        } else {
            return new ModelAndView(PagesUtil.SYS_UNAUTHORIZED);
        }
    }

    //Màn hình root
    @GetMapping("/document")
    public ModelAndView getRootDocument() {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.SYS_LOGIN);
        }
        if (validateModuleStorage.read()) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.STG_DOCUMENT);
            List<Document> listRootDocument = documentService.findRootDocument();
            for (int i = 0; i < listRootDocument.size(); i++) {
                listRootDocument.get(i).setCreatedAt(FlowieeUtil.formatDate(listRootDocument.get(i).getCreatedAt(),"dd/MM/yyyy"));
            }
            modelAndView.addObject("listDocument", listRootDocument);
            modelAndView.addObject("document", new Document());
            //select-option danh sách loại tài liệu
            List<Category> listLoaiTaiLieu = new ArrayList<>();
            listLoaiTaiLieu.add(categoryService.findSubCategoryDefault(AppConstants.DOCUMENTTYPE));
            listLoaiTaiLieu.addAll(categoryService.findSubCategoryUnDefault(AppConstants.DOCUMENTTYPE));
            modelAndView.addObject("listLoaiTaiLieu", listLoaiTaiLieu);
            //select-option danh sách thư mục
            List<Document> listFolder = new ArrayList<>();
            listFolder.add(new Document(0, "--Chọn thư mục--"));
            listFolder.addAll(documentService.findAllFolder());
            modelAndView.addObject("listFolder", listFolder);
            //Parent name
            modelAndView.addObject("documentParentName", "KHO TÀI LIỆU");            
            if (validateModuleStorage.insert()) {
                modelAndView.addObject("action_create", "enable");
            }
            if (validateModuleStorage.update()) {
                modelAndView.addObject("action_update", "enable");
            }
            if (validateModuleStorage.delete()) {
                modelAndView.addObject("action_delete", "enable");
            }
            return baseView(modelAndView);
        } else {
            return new ModelAndView(PagesUtil.SYS_UNAUTHORIZED);
        }
    }

    @GetMapping("/document/{aliasPath}")
    public ModelAndView getListDocument(@PathVariable("aliasPath") String aliasPath) {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.SYS_LOGIN);
        }
        String aliasName = FlowieeUtil.getAliasNameFromAliasPath(aliasPath);
        int documentId = FlowieeUtil.getIdFromAliasPath(aliasPath);
        Document document = documentService.findById(documentId);
        if (!(aliasName + "-" + documentId).equals(document.getAliasName() + "-" + document.getId())) {
            throw new NotFoundException("Document not found!");
        }
        //Kiểm tra quyền xem document
        if (!validateModuleStorage.read() || !docShareService.isShared(documentId)) {
            return new ModelAndView(PagesUtil.SYS_UNAUTHORIZED);
        }
        if (document.getLoai().equals(AppConstants.DOCUMENT_TYPE.FI.name())) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.STG_DOCUMENT_DETAIL);
            modelAndView.addObject("docDetail", document);
            modelAndView.addObject("document", new Document());
            //load metadata
            List<DocMetaDTO> docMetaDTO = documentService.getMetadata(documentId);
            modelAndView.addObject("listDocDataInfo", docMetaDTO);
            //Load file active
            modelAndView.addObject("fileActiveOfDocument", fileStorageService.findFileIsActiveOfDocument(documentId));
            //Load các version khác của document
            modelAndView.addObject("listFileOfDocument", fileStorageService.getFileOfDocument(documentId));            
            //Cây thư mục
            //modelAndView.addObject("folders", list);
            if (validateModuleStorage.update()) {
                modelAndView.addObject("action_update", "enable");
            }
            if (validateModuleStorage.delete()) {
                modelAndView.addObject("action_delete", "enable");
            }
            return baseView(modelAndView);
        }

        if (document.getLoai().equals(AppConstants.DOCUMENT_TYPE.FO.name())) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.STG_DOCUMENT);
            modelAndView.addObject("document", new Document());
            modelAndView.addObject("listDocument", documentService.findDocumentByParentId(documentId));
            //select-option danh loại tài liệu
            List<Category> listLoaiTaiLieu = new ArrayList<>();
            listLoaiTaiLieu.add(categoryService.findSubCategoryDefault(AppConstants.DOCUMENTTYPE));
            listLoaiTaiLieu.addAll(categoryService.findSubCategoryUnDefault(AppConstants.DOCUMENTTYPE));
            modelAndView.addObject("listLoaiTaiLieu", listLoaiTaiLieu);
            //select-option danh sách thư mục
            List<Document> listFolder = new ArrayList<>();
            listFolder.add(documentService.findById(documentId));
            listFolder.addAll(documentService.findAllFolder());
            modelAndView.addObject("listFolder", listFolder);
            //Parent name
            modelAndView.addObject("documentParentName", document.getTen().toUpperCase());            
            if (validateModuleStorage.insert()) {
                modelAndView.addObject("action_create", "enable");
            }
            if (validateModuleStorage.update()) {
                modelAndView.addObject("action_update", "enable");
            }
            if (validateModuleStorage.delete()) {
                modelAndView.addObject("action_delete", "enable");
            }

            return baseView(modelAndView);
        }

        return new ModelAndView();
    }

    //Insert FILE và FOLDER
    @PostMapping("/document/insert")
    public String insert(HttpServletRequest request,
                         @ModelAttribute("document") Document document,
                         @RequestParam(name = "file", required = false) MultipartFile file) throws IOException {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        document.setAliasName(FlowieeUtil.generateAliasName(document.getTen()));
        document.setCreatedBy(accountService.findCurrentAccountId());
        if (document.getParentId() == null) {
            document.setParentId(0);
        }
        Document documentSaved = documentService.saveReturnEntity(document);
        //Trường hợp document được tạo mới là file upload
        if (document.getLoai().equals(AppConstants.DOCUMENT_TYPE.FI.name()) && file != null) {
            //Lưu file đính kèm vào thư mục chứ file upload
            fileStorageService.saveFileOfDocument(file, documentSaved.getId());

            //Lưu giá trị default vào DocData
            List<DocField> listDocField = docFieldService.findByDocTypeId(document.getLoaiTaiLieu().getId());
            for (DocField docField : listDocField) {
                DocData docData = DocData.builder()
                        .docField(new DocField(docField.getId()))
                        .document(new Document(document.getId()))
                        .noiDung("").build();
                docDataService.save(docData);
            }
        }
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/document/change-file/{id}")
    public String changeFile(@RequestParam("file") MultipartFile file,
                             @PathVariable("id") Integer documentId,
                             HttpServletRequest request) throws IOException {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        fileStorageService.changFileOfDocument(file, documentId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/document/update/{id}")
    public String update(@ModelAttribute("document") Document document,
                         @PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (document == null || documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        documentService.update(document, documentId);
        return "redirect:" + request.getHeader("referer");
    }

    @GetMapping("/document/update-metadata/{id}")
    public String updateMetadata(HttpServletRequest request,
                                 @PathVariable("id") Integer documentId,
                                 @RequestParam("docDataId") Integer[] docDataIds,
                                 @RequestParam("docDataValue") String[] docDataValues) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        documentService.updateMetadata(docDataIds, docDataValues, documentId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/document/delete/{id}")
    public String deleteDocument(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        documentService.delete(documentId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/document/move/{id}")
    public String moveDocument(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/document/share/{id}")
    public String share(@PathVariable("id") Integer documentId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (documentId <= 0 || documentService.findById(documentId) == null) {
            throw new NotFoundException("Document not found!");
        }
        return "redirect:" + request.getHeader("referer");
    }
    
    @PostMapping("/docfield/insert")
    public String insertDocfield(DocField docField, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (!validateModuleStorage.update()) {
            return PagesUtil.SYS_UNAUTHORIZED;
        }
        docField.setTrangThai(false);
        docFieldService.save(docField);
        return "redirect:" + request.getHeader("referer");
    }
    
    @PostMapping(value = "/docfield/update/{id}", params = "update")
    public String updateDocfield(HttpServletRequest request,
                                 @ModelAttribute("docField") DocField docField,
                                 @PathVariable("id") Integer docFieldId) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (!validateModuleStorage.update()) {
            return PagesUtil.SYS_UNAUTHORIZED;
        }
        if (docFieldId <= 0 || documentService.findById(docFieldId) == null) {
            throw new NotFoundException("Docfield not found!");
        }
        docFieldService.update(docField, docFieldId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/docfield/delete/{id}")
    public String deleteDocfield(@PathVariable("id") int docfiledId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (!validateModuleStorage.delete()) {
            return PagesUtil.SYS_UNAUTHORIZED;
        }
        if (docFieldService.findById(docfiledId) == null){
            throw new NotFoundException("Docfield not found!");
        }
        docFieldService.delete(docfiledId);
        return "redirect:" + request.getHeader("referer");
    }
}