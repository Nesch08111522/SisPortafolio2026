import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

const API = '/api/v1';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  countries() { return this.http.get<Country[]>(`${API}/catalog/geography/countries`); }
  regions(countryId: string) { return this.http.get<Named[]>(`${API}/catalog/geography/regions`, { params: { countryId } }); }
  provinces(regionId: string) { return this.http.get<Named[]>(`${API}/catalog/geography/provinces`, { params: { regionId } }); }
  cities(countryId: string, regionId?: string) {
    let params = new HttpParams().set('countryId', countryId);
    if (regionId) params = params.set('regionId', regionId);
    return this.http.get<Named[]>(`${API}/catalog/geography/cities`, { params });
  }
  districts(cityId: string) { return this.http.get<Named[]>(`${API}/catalog/geography/districts`, { params: { cityId } }); }
  faculties() { return this.http.get<Faculty[]>(`${API}/catalog/faculties`); }
  careers(facultyId?: string, q = '') {
    let params = new HttpParams().set('size', '100');
    if (facultyId) params = params.set('facultyId', facultyId);
    if (q) params = params.set('q', q);
    return this.http.get<Page<Career>>(`${API}/catalog/careers`, { params });
  }
  career(id: string) { return this.http.get<CareerDetail>(`${API}/catalog/careers/${id}`); }
  universities(q = '') { return this.http.get<Page<Named>>(`${API}/catalog/universities`, { params: { q, size: '20' } }); }
  recommended(careerId?: string) {
    const params = careerId ? new HttpParams().set('careerId', careerId) : undefined;
    return this.http.get<TemplateCard[]>(`${API}/catalog/templates/recommended`, { params });
  }
  profile() { return this.http.get<ProfileResponse>(`${API}/profiles/me`); }
  saveProfile(body: unknown) { return this.http.put<ProfileResponse>(`${API}/profiles/me`, body); }
  saveAcademic(body: unknown) { return this.http.put<ProfileResponse>(`${API}/academic/me`, body); }
  dashboard() { return this.http.get<Dashboard>(`${API}/dashboard`); }
  editor() { return this.http.get<EditorState>(`${API}/portfolios/me`); }
  preview() { return this.http.get<PortfolioView>(`${API}/portfolios/me/preview`); }
  saveSettings(body: unknown) { return this.http.put(`${API}/portfolios/me`, body); }
  saveSections(body: unknown) { return this.http.put(`${API}/portfolios/me/sections`, body); }
  applyTemplate(templateId: string) { return this.http.post(`${API}/portfolios/me/template`, { templateId }); }
  publish(body: unknown) { return this.http.post(`${API}/portfolios/me/publish`, body); }
  unpublish() { return this.http.post(`${API}/portfolios/me/unpublish`, {}); }
  slug(slug: string) { return this.http.get<{ slug: string; available: boolean }>(`${API}/portfolios/slug-availability`, { params: { slug } }); }
  version(label: string) { return this.http.post(`${API}/portfolios/me/versions`, { label }); }
  restore(id: string) { return this.http.post(`${API}/portfolios/me/versions/${id}/restore`, {}); }
  domain(domain: string) { return this.http.post(`${API}/portfolios/me/domains`, { domain }); }
  deleteSection(key: string) { return this.http.delete(`${API}/portfolios/me/sections/${key}`); }
  qr() { return this.http.get(`${API}/portfolios/me/qr`, { responseType: 'blob' }); }
  setUserEnabled(id: string, enabled: boolean) { return this.http.patch(`${API}/admin/users/${id}`, { enabled }); }
  createFaculty(body: unknown) { return this.http.post(`${API}/admin/faculties`, body); }
  createCareer(body: unknown) { return this.http.post(`${API}/admin/careers`, body); }
  createUniversity(body: unknown) { return this.http.post(`${API}/admin/universities`, body); }
  moderate(id: string, reason: string) { return this.http.post(`${API}/moderation/portfolios/${id}/unpublish`, { reason }); }
  projects() { return this.http.get<ProjectItem[]>(`${API}/projects`); }
  saveProject(body: unknown, id?: string) { return id ? this.http.put(`${API}/projects/${id}`, body) : this.http.post(`${API}/projects`, body); }
  deleteProject(id: string) { return this.http.delete(`${API}/projects/${id}`); }
  courses() { return this.http.get<CourseItem[]>(`${API}/courses`); }
  saveCourse(body: unknown, id?: string) { return id ? this.http.put(`${API}/courses/${id}`, body) : this.http.post(`${API}/courses`, body); }
  deleteCourse(id: string) { return this.http.delete(`${API}/courses/${id}`); }
  certifications() { return this.http.get<CertItem[]>(`${API}/certifications`); }
  saveCertification(body: unknown, id?: string) { return id ? this.http.put(`${API}/certifications/${id}`, body) : this.http.post(`${API}/certifications`, body); }
  skills() { return this.http.get<SkillItem[]>(`${API}/skills`); }
  addSkill(body: unknown) { return this.http.post(`${API}/skills`, body); }
  removeSkill(id: string) { return this.http.delete(`${API}/skills/${id}`); }
  experiences() { return this.http.get<ExperienceItem[]>(`${API}/experiences`); }
  saveExperience(body: unknown, id?: string) { return id ? this.http.put(`${API}/experiences/${id}`, body) : this.http.post(`${API}/experiences`, body); }
  publications() { return this.http.get<PublicationItem[]>(`${API}/publications`); }
  savePublication(body: unknown, id?: string) { return id ? this.http.put(`${API}/publications/${id}`, body) : this.http.post(`${API}/publications`, body); }
  documents() { return this.http.get<DocumentItem[]>(`${API}/documents`); }
  upload(form: FormData) { return this.http.post(`${API}/documents`, form); }
  process(id: string) { return this.http.post<Extraction>(`${API}/documents/${id}/process`, {}); }
  confirm(documentId: string, extractionId: string, body: unknown) { return this.http.post(`${API}/documents/${documentId}/extractions/${extractionId}/confirm`, body); }
  publicPortfolio(slug: string) { return this.http.get<PortfolioView>(`${API}/public/${slug}`); }
  examples() { return this.http.get<ExampleCard[]>(`${API}/public/examples`); }
  visit(slug: string, body: unknown) { return this.http.post(`${API}/public/${slug}/visits`, body); }
  contact(slug: string, body: unknown) { return this.http.post(`${API}/public/${slug}/contact`, body); }
  messages() { return this.http.get<ContactMessage[]>(`${API}/contacts`); }
  notifications() { return this.http.get<Notice[]>(`${API}/notifications`); }
  adminUsers() { return this.http.get<Page<AdminUser>>(`${API}/admin/users`); }
  adminReports() { return this.http.get<Record<string, number>>(`${API}/admin/reports`); }
  verify(token: string) { return this.http.post(`${API}/auth/verify-email`, { token }); }
  forgot(email: string) { return this.http.post(`${API}/auth/forgot-password`, { email }); }
  reset(body: unknown) { return this.http.post(`${API}/auth/reset-password`, body); }
  resend(email: string) { return this.http.post(`${API}/auth/resend-verification`, { email }); }
}

export interface Country { id: string; iso2: string; name: string; }
export interface Named { id: string; name: string; }
export interface Faculty { id: string; code: string; name: string; description: string; }
export interface Career { id: string; code: string; name: string; nameEn: string; iscedCode: string; }
export interface CareerDetail extends Career { specializations: Named[]; faculties: Faculty[]; description: string; }
export interface Page<T> { content: T[]; totalElements: number; }
export interface TemplateCard {
  templateId: string; code: string; name: string; description: string; layout: string; variantName: string;
  sections: SectionSchema[]; competencies: string[]; examples: string[]; disclaimer?: string; theme?: Theme;
}
export interface SectionSchema { key: string; title: string; component: string; variant: string; hint: string; }
export interface ProfileResponse { firstName: string; lastName: string; email: string; onboardingCompleted: boolean; personal: Personal; professional: Professional; academic: Academic | null; }
export interface Personal {
  bio?: string; bioEn?: string; birthDate?: string; showBirthDate: boolean; countryId?: string; regionId?: string; provinceId?: string;
  cityId?: string; districtId?: string; customRegion?: string; customProvince?: string; customCity?: string; customDistrict?: string;
  address?: string; phone?: string; professionalEmail?: string; locationLabel?: string;
}
export interface Professional {
  linkedin?: string; github?: string; gitlab?: string; behance?: string; dribbble?: string; orcid?: string; googleScholar?: string; website?: string;
}
export interface Academic {
  universityId?: string; universityName?: string; facultyId?: string; careerId?: string; specializationId?: string; semester?: string;
  admissionYear?: number; expectedGraduation?: string; status?: string; relevantCourses?: string; interests?: string; professionalGoals?: string; licenseDeclared: boolean;
}
export interface Dashboard { completion: number; recommendations: string[]; publicationStatus: string; slug?: string; publicUrl?: string; visits: number; projects: number; courses: number; certifications: number; }
export interface EditorState { portfolio: PortfolioSummary; sections: SectionState[]; versions: VersionItem[]; }
export interface PortfolioSummary { id: string; title: string; slug?: string; status: string; templateId?: string; theme: Theme; version: number; bilingual: boolean; whatsappEnabled: boolean; showEmail: boolean; showPhone: boolean; analyticsEnabled: boolean; recruiterViewEnabled: boolean; showcaseOptIn: boolean; seoTitle?: string; seoDescription?: string; publicUrl?: string; }
export interface Theme { accent?: string; accent2?: string; background?: string; surface?: string; text?: string; muted?: string; fontHeading?: string; fontBody?: string; mode?: string; radius?: string; buttonStyle?: string; animation?: string; }
export interface SectionState { sectionKey: string; title: string; component: string; variant?: string; visible: boolean; sortOrder: number; content: Record<string, unknown>; }
export interface VersionItem { id: string; label: string; createdAt: string; }
export interface PortfolioView {
  layout: string; templateCode: string; theme: Theme; bilingual: boolean; recruiterViewEnabled: boolean; disclaimer?: string;
  whatsappEnabled: boolean; whatsappNumber?: string; sections: SectionState[]; person: PersonView; academicLabel?: string;
  education?: EducationView; projects: ProjectView[]; courses: CourseView[]; certifications: CertView[]; experiences: ExperienceView[];
  publications: PublicationView[]; skills: SkillItem[]; seo: { title: string; description: string; canonicalUrl: string; jsonLd: unknown };
}
export interface PersonView { firstName: string; lastName: string; bio?: string; bioEn?: string; photoUrl?: string; location?: string; email?: string; phone?: string; links: { label: string; url: string }[]; }
export interface EducationView { university?: string; career?: string; status?: string; semester?: string; interests?: string; goals?: string; }
export interface ProjectView { id: string; name: string; description?: string; descriptionEn?: string; category?: string; role?: string; results?: string; architectureNote?: string; links?: Record<string, string>; technologies: string[]; images: string[]; coverUrl?: string; }
export interface CourseView { id: string; name: string; institution: string; status: string; hours: number; description: string; descriptionEn: string; }
export interface CertView { id: string; name: string; issuer?: string; issuedOn?: string; description?: string; verificationNote: string; }
export interface ExperienceView { id: string; title: string; organization?: string; kind: string; description?: string; descriptionEn?: string; }
export interface PublicationView { id: string; title: string; authors?: string; venue?: string; url?: string; kind: string; summary?: string; summaryEn?: string; }
export interface ProjectItem { id: string; name: string; description?: string; category?: string; publicationStatus: string; }
export interface CourseItem { id: string; name: string; institution?: string; status: string; hours?: number; description?: string; }
export interface CertItem { id: string; name: string; issuer?: string; description?: string; }
export interface SkillItem { id: string; name: string; category: string; note: string; }
export interface ExperienceItem { id: string; title: string; organization?: string; kind: string; description?: string; }
export interface PublicationItem { id: string; title: string; kind: string; summary?: string; url?: string; }
export interface DocumentItem { id: string; originalName: string; category: string; visibility: string; scanStatus: string; scanDetail?: string; }
export interface Extraction { id: string; suggestion: Record<string, unknown>; }
export interface ExampleCard { slug: string; title: string; name: string; seoDescription?: string; }
export interface ContactMessage { id: string; name: string; email: string; message: string; createdAt: string; }
export interface Notice { id: string; title: string; body: string; readAt?: string; }
export interface AdminUser { id: string; email: string; firstName: string; lastName: string; enabled: boolean; roles: string[]; }
